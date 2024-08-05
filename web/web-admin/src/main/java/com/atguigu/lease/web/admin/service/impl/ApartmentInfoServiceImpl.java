package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private ApartmentFacilityService facilityService;

    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private ApartmentLabelService labelService;

    @Autowired
    private ApartmentFeeValueService feeValueService;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;


    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate=apartmentSubmitVo.getId()!=null;//todo 必须先进性判断 防止是插入操作 id自增之后回显给Java对象
        super.saveOrUpdate(apartmentSubmitVo);//todo 调用父类的方法 将vo进行数据库操作
        //todo 上面的saveOrUpdate是针对ApartmentInfoMapper进行的操作 但是传入的是vo vo中包含的比ApartmentInfoMapper中的属性信息多
        //      多出的信息单独处理
        if(isUpdate){
            //todo 更新先删除在插入

            //todo 图片信息删除的时候需要指明图片的类型 有两种类型 不指明类型的话可能会误删除
            LambdaQueryWrapper<GraphInfo>graphInfoLambdaQueryWrapper=new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(graphInfoLambdaQueryWrapper);

            LambdaQueryWrapper<ApartmentFacility>facilityLambdaQueryWrapper=new LambdaQueryWrapper<>();
            facilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            facilityService.remove(facilityLambdaQueryWrapper);

            LambdaQueryWrapper<ApartmentLabel>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            labelService.remove(lambdaQueryWrapper);

            LambdaQueryWrapper<ApartmentFeeValue>feeValueLambdaQueryWrapper=new LambdaQueryWrapper<>();
            feeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            feeValueService.remove(feeValueLambdaQueryWrapper);


        }
        //todo 插入操作是更新和插入都需要的
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        //todo vo->Info
        ArrayList<GraphInfo>graphInfoArrayList=new ArrayList<>();
        for (GraphVo graphVo : graphVoList) {
            GraphInfo graphInfo=new GraphInfo();
            graphInfo.setItemType(ItemType.APARTMENT);
            graphInfo.setId(apartmentSubmitVo.getId());
            graphInfo.setName(graphVo.getName());
            graphInfo.setUrl(graphVo.getUrl());
            graphInfoArrayList.add(graphInfo);
        }
        graphInfoService.saveBatch(graphInfoArrayList);

        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)) {
            ArrayList<ApartmentFacility> facilityArrayList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityInfoId);
                facilityArrayList.add(apartmentFacility);
            }
            facilityService.saveBatch(facilityArrayList);
        }

        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)) {
            List<ApartmentLabel> apartmentLabelList = new ArrayList<>();
            for (Long labelId : labelIds) {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                apartmentLabelList.add(apartmentLabel);
            }
            labelService.saveBatch(apartmentLabelList);
        }


        //4.插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            feeValueService.saveBatch(apartmentFeeValueList);
        }


    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page,queryVo);

    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //todo 查询公寓基本信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //todo 查询公寓照片 返回的是vo数据 graphInfo中的信息太多不需要
//        LambdaQueryWrapper<GraphInfo>graphInfoLambdaQueryWrapper=new LambdaQueryWrapper<>();
//        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,ItemType.APARTMENT);
//        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
//        graphInfoService.list(graphInfoLambdaQueryWrapper);
        List<GraphVo>graphVoList=graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT,id);

        //todo 查询标签列表
        List<LabelInfo>labelInfoList=labelInfoMapper.selectListByApartmentId(id);

        //todo 查询配套信息
        List<FacilityInfo>facilityInfoList=facilityInfoMapper.selectListByApartmentId(id);

        //todo 查询花费信息
        List<FeeValueVo>feeValueVoList=feeValueMapper.selectListByApartmentId(id);

        ApartmentDetailVo apartmentDetailVo=new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);

        return apartmentDetailVo;
    }

    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo>roomInfoLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getApartmentId,id);
        Long count = roomInfoMapper.selectCount(roomInfoLambdaQueryWrapper);

        if(count>0){
            //todo 公寓下面有房间
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }




        super.removeById(id);
        LambdaQueryWrapper<GraphInfo>graphInfoLambdaQueryWrapper=new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphInfoLambdaQueryWrapper);

        LambdaQueryWrapper<ApartmentFacility>facilityLambdaQueryWrapper=new LambdaQueryWrapper<>();
        facilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        facilityService.remove(facilityLambdaQueryWrapper);

        LambdaQueryWrapper<ApartmentLabel>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        labelService.remove(lambdaQueryWrapper);

        LambdaQueryWrapper<ApartmentFeeValue>feeValueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        feeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        feeValueService.remove(feeValueLambdaQueryWrapper);
    }
}




