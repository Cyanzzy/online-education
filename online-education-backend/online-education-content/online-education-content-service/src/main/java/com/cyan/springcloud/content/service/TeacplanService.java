package com.cyan.springcloud.content.service;

import com.cyan.springcloud.model.dto.BindTeachplanMediaDto;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.TeachplanMedia;

import java.util.List;

/**
 * 课程计划管理逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-06-11
 */
public interface TeacplanService {

    /**
     * 课程计划查询
     *
     * @param courseId 课程id
     * @return
     */
    List<TeachplanDto> selectTreeNodes(Long courseId);

    /**
     * 新增/修改/保存课程计划
     *
     * @param saveTeachplanDto
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     *
     * @param teachPlanId 课程计划id
     */
    void deleteTeachplan(Long teachPlanId);

    /**
     * 课程计划排序
     *
     * @param moveType 移动类型  movedown表示下移 moveup表示上移
     * @param teachPlanId 课程计划表id
     */
    void orderByTeachplan(String moveType, Long teachPlanId);

    /**
     * 教学计划绑定媒资
     *
     * @param bindTeachplanMediaDto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 解绑教学计划与媒资信息
     *
     * @param teachPlanId       教学计划id
     * @param mediaId           媒资信息id
     */
    void unassociationMedia(Long teachPlanId, Long mediaId);
}
