package com.cyan.springcloud.search.service.impl;

import com.alibaba.fastjson.JSON;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.search.dto.SearchCourseParamDto;
import com.cyan.springcloud.search.dto.SearchPageResultDto;
import com.cyan.springcloud.search.po.CourseIndex;
import com.cyan.springcloud.search.service.CourseSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程搜索service实现类
 * @date 2022/9/24 22:48
 */
@Slf4j
@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Value("${elasticsearch.course.source_fields}")
    private String sourceFields;

    @Resource
    private RestHighLevelClient client;

    @Override
    public SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto courseSearchParam) {

        // 设置索引
        SearchRequest searchRequest = new SearchRequest(courseIndexStore);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // source源字段过虑
        String[] sourceFieldsArray = sourceFields.split(",");
        searchSourceBuilder.fetchSource(sourceFieldsArray, new String[]{});
        if (courseSearchParam == null) {
            courseSearchParam = new SearchCourseParamDto();
        }
        // 关键字
        if (StringUtils.isNotEmpty(courseSearchParam.getKeywords())) {
            // 匹配关键字
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeywords(), "name", "description");
            // 设置匹配占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            // 提升另个字段的Boost值
            multiMatchQueryBuilder.field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        // 过虑
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mtName", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("stName", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        // 分页
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        int start = (int) ((pageNo - 1) * pageSize);
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(Math.toIntExact(pageSize));
        // 布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        // 请求搜索
        searchRequest.source(searchSourceBuilder);
        // 聚合设置
        buildAggregation(searchRequest);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("课程搜索异常：{}", e.getMessage());
            return new SearchPageResultDto<CourseIndex>(new ArrayList(), 0, 0, 0);
        }

        // 结果集处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 记录总数
        TotalHits totalHits = hits.getTotalHits();
        // 数据列表
        List<CourseIndex> list = new ArrayList<>();

        for (SearchHit hit : searchHits) {

            String sourceAsString = hit.getSourceAsString();
            CourseIndex courseIndex = JSON.parseObject(sourceAsString, CourseIndex.class);

            // 取出source
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            // 课程id
            Long id = courseIndex.getId();
            // 取出名称
            String name = courseIndex.getName();
            // 取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();

                }
            }
            courseIndex.setId(id);
            courseIndex.setName(name);

            list.add(courseIndex);

        }
        SearchPageResultDto<CourseIndex> pageResult = new SearchPageResultDto<>(list, totalHits.value, pageNo, pageSize);

        // 获取聚合结果
        List<String> mtList = getAggregation(searchResponse.getAggregations(), "mtAgg");
        List<String> stList = getAggregation(searchResponse.getAggregations(), "stAgg");

        pageResult.setMtList(mtList);
        pageResult.setStList(stList);

        return pageResult;
    }


    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("mtAgg")
                .field("mtName")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("stAgg")
                .field("stName")
                .size(100)
        );

    }

    private List<String> getAggregation(Aggregations aggregations, String aggName) {
        // 4.1.根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        // 4.2.获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 4.3.遍历
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            // 4.4.获取key
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

//    /**
//     * 根据分页进行搜索
//     */
//    public SearchPageResultDto<CourseIndex> queryCoursePubIndexByPage(PageParams pageParams, SearchCourseParamDto courseSearchParam) {
//
//        // 1. 准备Request对象
//        SearchRequest request = new SearchRequest(courseIndexStore);
//        // 2. 组织DSL参数，这里使用布尔查询
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        String[] sourceFieldsArray = sourceFields.split(",");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // sourceFieldsArray指定要返回的字段，new String[]{}指定不返回的字段
//        searchSourceBuilder.fetchSource(sourceFieldsArray, new String[]{});
//        // 3. 分页
//        Long pageNo = pageParams.getPageNo();
//        Long pageSize = pageParams.getPageSize();
//        // 3.1 指定起始查询位置和查询条数
//        int start = (int) ((pageNo - 1) * pageSize);
//        searchSourceBuilder.from(start)
//                .size(Math.toIntExact(pageSize));
//        // 4. 布尔查询
//        searchSourceBuilder.query(boolQuery);
//        request.source(searchSourceBuilder);
//        // 5. 发送请求，获取响应结果
//        SearchResponse response = null;
//        try {
//            response = client.search(request, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            log.debug("课程搜索异常：{}", e.getMessage());
//            return new SearchPageResultDto<>(new ArrayList<>(), 0, 0, 0);
//        }
//        // 6. 解析响应
//        SearchHits searchHits = response.getHits();
//        // 6.1 获取总条数
//        long totalHits = searchHits.getTotalHits().value;
//        // 6.2 获取文档数组
//        SearchHit[] hits = searchHits.getHits();
//        ArrayList<CourseIndex> list = new ArrayList<>();
//        // 6.3 遍历
//        for (SearchHit hit : hits) {
//            // 获取文档source
//            String jsonCourseString = hit.getSourceAsString();
//            // 转为CourseIndex对象，加入到集合中
//            CourseIndex courseIndex = JSON.parseObject(jsonCourseString, CourseIndex.class);
//            list.add(courseIndex);
//        }
//        // 7. 封装结果
//        SearchPageResultDto<CourseIndex> pageResult = new SearchPageResultDto<>(list, totalHits, pageNo, pageSize);
//        return pageResult;
//    }
//
//    /**
//     * 根据关键字、一级分类、二级分类、难度等级搜索
//     */
//    public SearchPageResultDto<CourseIndex> queryCoursePubIndexByCondition(PageParams pageParams, SearchCourseParamDto courseSearchParam) {
//        // 1. 准备Request对象
//        SearchRequest request = new SearchRequest(courseIndexStore);
//        // 2. 组织DSL参数，这里使用布尔查询
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        String[] sourceFieldsArray = sourceFields.split(",");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // sourceFieldsArray指定要返回的字段，new String[]{}指定不返回的字段
//        searchSourceBuilder.fetchSource(sourceFieldsArray, new String[]{});
//        // 3. 分页
//        Long pageNo = pageParams.getPageNo();
//        Long pageSize = pageParams.getPageSize();
//        // 3.1 指定起始查询位置和查询条数
//        int start = (int) ((pageNo - 1) * pageSize);
//        searchSourceBuilder.from(start)
//                .size(Math.toIntExact(pageSize));
//        // 3.2 指定条件查询
//        if (courseSearchParam == null) {
//            courseSearchParam = new SearchCourseParamDto();
//        }
//        // 3.2.1 匹配关键字
//        if (StringUtils.isNotEmpty(courseSearchParam.getKeywords())) {
//            String keywords = courseSearchParam.getKeywords();
//            boolQuery.must(QueryBuilders
//                    .multiMatchQuery(keywords, "name", "description")
//                    .minimumShouldMatch("70%")
//                    .field("name", 10));
//        }
//        // 3.2.2 匹配大分类
//        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
//            boolQuery.filter(QueryBuilders
//                    .termQuery("mtName", courseSearchParam.getMt()));
//        }
//        // 3.2.3 匹配小分类
//        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
//            boolQuery.filter(QueryBuilders
//                    .termQuery("stName", courseSearchParam.getSt()));
//        }
//        // 3.2.4 匹配难度
//        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
//            boolQuery.filter(QueryBuilders
//                    .termQuery("grade", courseSearchParam.getGrade()));
//        }
//        // 4. 布尔查询
//        searchSourceBuilder.query(boolQuery);
//        request.source(searchSourceBuilder);
//        // 5. 发送请求，获取响应结果
//        SearchResponse response = null;
//        try {
//            response = client.search(request, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            log.debug("课程搜索异常：{}", e.getMessage());
//            return new SearchPageResultDto<>(new ArrayList<>(), 0, 0, 0);
//        }
//        // 6. 解析响应
//        SearchHits searchHits = response.getHits();
//        // 6.1 获取总条数
//        long totalHits = searchHits.getTotalHits().value;
//        // 6.2 获取文档数组
//        SearchHit[] hits = searchHits.getHits();
//        ArrayList<CourseIndex> list = new ArrayList<>();
//        // 6.3 遍历
//        for (SearchHit hit : hits) {
//            // 获取文档source
//            String jsonCourseString = hit.getSourceAsString();
//            // 转为CourseIndex对象，加入到集合中
//            CourseIndex courseIndex = JSON.parseObject(jsonCourseString, CourseIndex.class);
//            list.add(courseIndex);
//        }
//        // 7. 封装结果
//        SearchPageResultDto<CourseIndex> pageResult = new SearchPageResultDto<>(list, totalHits, pageNo, pageSize);
//        return pageResult;
//    }

}
