package com.rblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.common.BaseContext;
import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.dto.MaterialDto;
import com.rblue.dto.OutformDto;
import com.rblue.entity.*;
import com.rblue.mapper.OutformMapper;
import com.rblue.service.*;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutformServiceImpl extends ServiceImpl<OutformMapper, Outform> implements OutformService {


    @Autowired
    private OutformMaterialService outformMaterialService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private WarehouseService warehouseService;

    //添加新的出库单
    public String saveOutform(OutformDto outformDto) {
        //保存出库信息
        Outform outform = new Outform();
        BeanUtils.copyProperties(outformDto, outform);
        //创建雪花算法Id
        Long outformId = IdWorker.getId();
        outform.setId(outformId);
        System.out.println("--------------------------" + outformId);

        //获取所有的材料信息，并保存到出库材料表中
        List<OutformMaterial> outformMaterials = new ArrayList<>();
        //获取所有的材料信息
        //List<Material> materials = new ArrayList<>();

        for (MaterialDto item : outformDto.getMaterials()) {

            //获取材料id
            Long materialId = item.getId();
            //获取出库量
            Double amount = item.getAmount();
            //获取领料次数
            Integer number = item.getNumber();
            //校验领料量是否超过库存
          Material materialCheck=  materialService.getById(materialId);
            //创建出库材料对象
            OutformMaterial outformMaterial = new OutformMaterial();
            if (amount == null || number == null) {
                //存在元素为空的情况
                return "当前有材料还未填写出库信息";
            } else {
                if (amount > materialCheck.getAvailable()) {
                    return "当前取料已经超出库存";
                } else{
                        outformMaterial.setOutformId(outformId);
                        outformMaterial.setMaterialId(materialId);
                        outformMaterial.setAmount(amount);
                        outformMaterial.setNumber(number);
                        //更新材料库存
                   // materialCheck.setAvailable(materialCheck.getAvailable() - amount);
                        //更新数据
                           // materials.add(materialCheck);
                            outformMaterials.add(outformMaterial);

                }
            }
        }//for
        //保存出库信息到出库表
        this.save(outform);
        //保存到出库材料中
        outformMaterialService.saveBatch(outformMaterials);
        //更新材料库存
      //  materialService.updateBatchById(materials);
        return "success";
        }

        //查询所有记录
    public Page<OutformDto> selectAll(Long id, int page, int pageSize , String beginTime, String endTime, String isSubmit, String checkStatus,String orderByTime){
        //创建分页构造
        Page<Outform> outformPage = new Page<>(page, pageSize);
        Page<OutformDto> dtoPage = new Page<>();
        //创建条件构造器
        LambdaQueryWrapper<Outform> lqw = new LambdaQueryWrapper<>();
        //入库单id是否存在
        lqw.like(id != null, Outform::getId, id);
        //时间段是否存在
        if(beginTime!=null&&endTime!=null){
            LocalDateTime bTime= LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime eTime= LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            lqw.gt(Outform::getCreateTime,bTime).lt(Outform::getCreateTime,eTime);
        }
        //判断是否为未提交
        lqw.eq(isSubmit!=null,Outform::getIsSubmit,0);
        //审核情况是否存在
        if(checkStatus!=null) {
            lqw.ne(checkStatus.equals("未审核"), Outform::getCheckStatus, 1);//要求显示未审核的
            lqw.eq(checkStatus.equals("已审核"), Outform::getCheckStatus, 1); //要求显示已审核的,仓库管理员出库
            lqw.eq(checkStatus.equals("审核失败"), Outform::getCheckStatus, 2);//材料管理员出库单
        }

        //添加当前用户的id，未删除的、按照紧急程度排序
        Long loginId = BaseContext.getCurrentId();
        lqw.eq(Outform::getCreateUser, loginId).eq(Outform::getIsDeleted, 0);
        //判断是否按时间排序
        if(orderByTime!=null){
            lqw.orderByDesc(Outform::getCreateTime);
        }else{
            lqw.orderByDesc(Outform::getUrgentStatus);
        }

        //得到查询结果
        this.page(outformPage, lqw);
        //复制
        BeanUtils.copyProperties(outformPage, dtoPage, "records");
        //遍历查询结果获取创建人id并查看名字，插入到outformDto中
        List<OutformDto> dtoList = outformPage.getRecords().stream().map(item -> {
            //创建dto对象
            OutformDto dto = new OutformDto();
            //复制
            BeanUtils.copyProperties(item, dto);
            //获取创建人id
            Long createUser = item.getCreateUser();
            //查询得到姓名
            Manager manager = managerService.getById(createUser);
            String userName = manager.getName();
            //加入姓名
            dto.setUserName(userName);
            return dto;
        }).collect(Collectors.toList());
        //将记录存回dtoPage
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    //根据id查找
    public OutformDto selectById(Long id){
        //根据id查询出库单
       Outform outform= this.getById(id);
       //根据创建人查询信息
        Manager manager=new Manager();
        manager=managerService.getById(outform.getCreateUser());
       //根据出库单号获取所有的出库单材料信息
        LambdaQueryWrapper<OutformMaterial> lqw=new LambdaQueryWrapper<>();
        //添加入库单id条件
        lqw.eq(OutformMaterial::getOutformId,id);
       //查询得到所有的出库材料记录
        List<OutformMaterial> outformMaterials= outformMaterialService.list(lqw);
        //遍历得到材料信息
        List<MaterialDto> materialDtoList=outformMaterials.stream().map(item->{
            //获取材料 Id、出库量、和领料次数
            Long materialId=item.getMaterialId();
            Double amount=item.getAmount();
            Integer number=item.getNumber();
            Double backAmount=item.getBackAmount();
            MaterialDto dto=new MaterialDto();
            //根据id查询材料信息
           Material material= materialService.getById(materialId);
           //根据仓库Id查询仓库信息
          Long warehouseId=  material.getWarehouseId();
        Warehouse warehouse=  warehouseService.getById(warehouseId);
           //复制材料信息
            BeanUtils.copyProperties(material,dto);
            dto.setAmount(amount);
            dto.setNumber(number);
            dto.setBackAmount(backAmount);
            //设置warehouse
            dto.setWarehouse(warehouse);
            return dto;
        }).collect(Collectors.toList());

        OutformDto outformDto=new OutformDto();

        //复制出库信息
        BeanUtils.copyProperties(outform,outformDto);
        //设置材料信息
        outformDto.setMaterials(materialDtoList);
        //设置填写人
        outformDto.setUserName(manager.getName());
        return outformDto;
    }


    //更新添加的材料入库单
    @Override
    public String updateOutform(OutformDto outdto){
        //创建对象
        Outform outform=new Outform();
        //获取入库单id
        Long id=outdto.getId();
        //复制
        BeanUtils.copyProperties(outdto,outform);
        //更新订单
        this.updateById(outform);
        //根据入库单id去删除入库材料
        //添加条件
        LambdaQueryWrapper<OutformMaterial> lqw=new LambdaQueryWrapper<>();
        lqw.eq(OutformMaterial::getOutformId,id);
        outformMaterialService.remove(lqw);
        //获取当前更新后的材料信息
        List<MaterialDto> materials=outdto.getMaterials();

        //创建一个出库材料集合，装载出库材料信息
        List<OutformMaterial> lists= new ArrayList<>();
        //遍历获取材料id和判断当前是否有材料
        for(MaterialDto item:materials) {
            if (item.getAmount() == null || item.getNumber() == null) {
                return "请填写完整材料信息";
            }
            //创建In对象
            OutformMaterial out = new OutformMaterial();

            //设置材料单号
            out.setMaterialId(item.getId());
            //插入唯一的出库单号
            out.setOutformId(id);
            //设置出库量
            out.setAmount(item.getAmount());
            out.setNumber(item.getNumber());
            lists.add(out);
        }
        //保存到入库材料表中内
        outformMaterialService.saveBatch(lists);
        return "success";
    }

    //提交出库单
    public String submitOutform(Long[] ids) {
        //遍历所有的出库id
        for (Long id : ids) {
            Outform outform = new Outform();
            //获取出库单记录
            outform = this.getById(id);
            if (outform.getIsSubmit() == 0) {//当前出库单未提交
                //添加出库单id条件
                LambdaQueryWrapper<OutformMaterial> lqw = new LambdaQueryWrapper<>();
                lqw.eq(OutformMaterial::getOutformId, id);
                //根据id查询出库单的材料
                List<OutformMaterial> materials = outformMaterialService.list(lqw);
                //保存需要更新库存的材料
                List<Material> list = new ArrayList<>();

                //获取提交的所有材料，并更新库存
                for (OutformMaterial item : materials) {
                    //获取材料 Id、出库量
                    Long materialId = item.getMaterialId();
                    Double amount = item.getAmount();
                    //根据id查询材料信息
                    Material material = materialService.getById(materialId);
                    if (amount > material.getAvailable()) {
                        return material.getName() + "的取料量已经超出库存";
                    } else {
                        //判断当前材料是否满足推陈储新
                        //获取材料的名称，规格,查找相同的材料,库存>0的不同时间批次
                        String name = material.getName();
                        String specification = material.getSpecification();
                        //创建条件
                        LambdaQueryWrapper<Material> lqwMaterial = new LambdaQueryWrapper<>();
                        lqwMaterial.eq(Material::getName, name).eq(Material::getSpecification, specification).gt(Material::getAvailable, 0);
                        //按照创建时间排序
                        lqwMaterial.orderByAsc(Material::getCreateTime);
                        List<Material> materialList = materialService.list(lqwMaterial);
                        //当前要出库材料并不满足推陈储新
                        if (!material.equals(materialList.get(0))) {
                            return material.getName() + "，并不符合推陈储新原则，请重新选择";
                        }
                        //更新材料库存
                        material.setAvailable(material.getAvailable() - amount);
                        list.add(material);
                    }
                }
                //当前出库单的所有材料都符合出库原则
                materialService.updateBatchById(list);
                //将出库单改为提交状态
                outform.setIsSubmit(1);
                this.updateById(outform);
            }
            else{
                return "出库单："+id+"，已提交";
            }
        }
        return "success";
    }

    //返回材料到仓库
    public String back(OutformDto outformDto){
        //获取出库单Id
        Long outformId = outformDto.getId();
        if(outformDto.getBackStatus()==1){
            return "当前出库单已退过料，请勿重复提交";
        }
        //获取所有的材料
       List<MaterialDto> materialsDto=outformDto.getMaterials();
       //接收需要修改的材料记录
        List<Material> materials=new ArrayList<>();
        //接收要修改的出库材料
        List<OutformMaterial> outformMaterials=new ArrayList<>();

        //遍历所有的材料，判断是否有退料
        for(MaterialDto item:materialsDto){
            //获取材料Id
           Long materialId= item.getId();
           //判断是否有退料，且要小于取料量
            if(item.getBackAmount() > item.getAmount()){
                return item.getName()+"，的退料量过大";
            }
            if(item.getBackAmount()!=null){
                //设置出库材料对象
                OutformMaterial outformMaterial=new OutformMaterial();
                //添加条件
                LambdaQueryWrapper<OutformMaterial> lqw=new LambdaQueryWrapper<>();
                lqw.eq(OutformMaterial::getOutformId,outformId).eq(OutformMaterial::getMaterialId,materialId);
                //查询记录
                outformMaterial= outformMaterialService.getOne(lqw);

                //设置材料对象,查询材料信息
                Material material=materialService.getById(materialId);
                //更新材料,当前材料的总量-原来出库材料单中的材料原退量+最新要退料量
                material.setAvailable(material.getAvailable()-outformMaterial.getBackAmount()+item.getBackAmount());
                materials.add(material);

               //设置取料量
                outformMaterial.setBackAmount(item.getBackAmount());
                outformMaterials.add(outformMaterial);
            }

        }

        //更新所有记录
       materialService.updateBatchById(materials);
        outformMaterialService.updateBatchById(outformMaterials);
        //更新出库单，设置为退过料
        Outform outform=new Outform();
        outform.setId(outformId);
        outform.setBackStatus(1);
        this.updateById(outform);
        return "success";
    }
    //下载出库单
    public void download(Long id, HttpServletResponse response, String haveBack) throws IOException {
        OutformDto dto = new OutformDto();
        if (id != null) {
            //根据id 查询信息
            dto = this.selectById(id);
        }
        String outformId = String.valueOf(dto.getId());

        // 创建工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建第一页sheet
        HSSFSheet sheet = workbook.createSheet("sheet1");
        //存储最大列宽
        Map<Integer, Integer> maxWidth = new HashMap<>();


        //设置表单行
        HSSFRow formRow = sheet.createRow(0);//第一行为表单行
        formRow.setHeightInPoints(45);
        // 大标题合并单元格
        if (haveBack.equals("退料")) {
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 8);//合并第一行的0-7列
            sheet.addMergedRegion(cellRangeAddress);
        } else {
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 7);//合并第一行的0-7列
            sheet.addMergedRegion(cellRangeAddress);
        }

        //设置表单样式
        CellStyle formStyle = workbook.createCellStyle();
        formStyle.setWrapText(true);//自动换行
        formStyle.setAlignment(CellStyle.ALIGN_CENTER);//字体居中
        //设置字体，大小
        HSSFFont formFont = workbook.createFont();
        formFont.setFontName("黑体");
        formFont.setFontHeightInPoints((short) 24);//字体大小
        formFont.setBoldweight(Font.BOLDWEIGHT_BOLD);//加粗
        formStyle.setFont(formFont);

        if (haveBack.equals("退料")) {
            formRow.createCell(0).setCellValue("退料单");

        } else {
            formRow.createCell(0).setCellValue("出库单");
        }
        formRow.getCell(0).setCellStyle(formStyle);//将样式设置到表单行中

        //设置出库单信息行
        HSSFRow outformRow = sheet.createRow(1);//第一行为表单行
        outformRow.setHeightInPoints(30);

        //设置表单样式
        CellStyle outformStyle = workbook.createCellStyle();
        outformStyle.setWrapText(true);//自动换行
        outformStyle.setAlignment(CellStyle.ALIGN_LEFT);//字体居中
        //特别处理时间格式
        CreationHelper createHelper = workbook.getCreationHelper();
        outformStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyy-mm-dd hh:mm:ss"));

        //设置字体，大小
        HSSFFont outformFont = workbook.createFont();
        outformFont.setFontName("黑体");
        outformFont.setFontHeightInPoints((short) 10);//字体大小
        outformStyle.setFont(outformFont);
        String formId = String.valueOf(dto.getId());
        outformRow.createCell(0).setCellValue("出库单号:");
        outformRow.createCell(1).setCellValue(formId);

        outformRow.createCell(3).setCellValue("填写时间：");
        Date date = Date.from(dto.getCreateTime().atZone(ZoneId.systemDefault()).toInstant());//将localDateTime转为date类型
        outformRow.createCell(4).setCellValue(date);
        //初始化出库单信息的样式
        for (int i = 0; i < 4; i++) {
            if(i!=2){
                maxWidth.put(i, outformRow.getCell(i).getStringCellValue().getBytes().length * 256 + 200);
                outformRow.getCell(i).setCellStyle(outformStyle);//添加样式
            }
        }
        maxWidth.put(4, outformRow.getCell(4).getDateCellValue().toString().getBytes().length * 256 + 200);
        outformRow.getCell(4).setCellStyle(outformStyle);//添加样式

        //设置标题行
        HSSFRow titleRow = sheet.createRow(2);//第一行为标题行
        titleRow.setHeightInPoints(23);//设置行高
        //设置头部样式
        CellStyle headStyle = workbook.createCellStyle();
        headStyle.setWrapText(true);//自动换行
        headStyle.setAlignment(CellStyle.ALIGN_LEFT);
        //设置字体，大小
        HSSFFont font = workbook.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 10);//字体大小
        font.setColor(Font.COLOR_RED);
        headStyle.setFont(font);

        //记录每一行的长度
        List<Object> valueList = new ArrayList<>();
        //创建标题表格
        titleRow.createCell(0).setCellValue("序号");
        valueList.add("序号");
        titleRow.createCell(1).setCellValue("材料名称");
        valueList.add("材料名称");
        titleRow.createCell(2).setCellValue("材料规格");
        valueList.add("材料规格");

        titleRow.createCell(3).setCellValue("单位");
        valueList.add("单位");

        titleRow.createCell(4).setCellValue("取出量");
        valueList.add("取出量");

        titleRow.createCell(5).setCellValue("领料次数");
        valueList.add("领料次数");

        titleRow.createCell(6).setCellValue("存放仓库");
        valueList.add("存放仓库");

        titleRow.createCell(7).setCellValue("货架");
        valueList.add("货架");

        int index = 8;
        if (haveBack.equals("退料")) {
            titleRow.createCell(8).setCellValue("退料量");
            valueList.add("退料量");
            index = 9;
        }

        //初始化标题的列宽，字体
        for (int i = 0; i < index; i++) {
            int length = valueList.get(i).toString().getBytes().length * 256 + 200;
            if(maxWidth.get(i)!=null&&titleRow.getCell(i)!=null){
                //这里把宽度最大限制到15000
                if (length > 15000) {
                    length = 15000;
                }
                maxWidth.put(i, Math.max(length, maxWidth.get(i)));//表格和标题选一个较大的作为列宽
                titleRow.getCell(i).setCellStyle(headStyle);
            }else if(titleRow.getCell(i)!=null){
                maxWidth.put(i,length);
                titleRow.getCell(i).setCellStyle(headStyle);
            }


        }

        //设置内容样式
        CellStyle contentStyle = workbook.createCellStyle();

        contentStyle.setWrapText(true);//自动换行
        contentStyle.setAlignment(CellStyle.ALIGN_LEFT);
        //设置字体，大小
        HSSFFont font2 = workbook.createFont();
        font2.setFontName("黑体");
        font2.setFontHeightInPoints((short) 10);//字体大小
        contentStyle.setFont(font2);

        //获取数据，设置表格
        List<MaterialDto> list = dto.getMaterials();
        for (int i = 0; i < list.size(); i++) {
            //获取当前行
            int currentRow = sheet.getLastRowNum() + 1;
            HSSFRow datarow = sheet.createRow(currentRow);
            datarow.setHeightInPoints(23);//设置行高

            MaterialDto materialDto = list.get(i);
            //记录每一行的长度
            List<Object> valueList2 = new ArrayList<>();

            String val0 = String.valueOf(i + 1);
            datarow.createCell(0).setCellValue(val0);
            valueList2.add(val0);

            String val1 = materialDto.getName();
            datarow.createCell(1).setCellValue(val1);
            valueList2.add(val1);

            String val2 = materialDto.getSpecification();
            datarow.createCell(2).setCellValue(val2);
            valueList2.add(val2);

            String val3 = materialDto.getUnit();
            datarow.createCell(3).setCellValue(val3);
            valueList2.add(val3);

            String val4 = String.valueOf(materialDto.getAmount());
            datarow.createCell(4).setCellValue(val4);
            valueList2.add(val4);

            String val5 = String.valueOf(materialDto.getNumber());
            datarow.createCell(5).setCellValue(val5);
            valueList2.add(val5);

            String val6 = materialDto.getWarehouse().getName();
            datarow.createCell(6).setCellValue(val6);
            valueList2.add(val6);

            String val7 = materialDto.getShelf();
            datarow.createCell(7).setCellValue(val7);
            valueList2.add(val7);

            if (haveBack.equals("退料")) {
                String val8 = String.valueOf(materialDto.getBackAmount());
                datarow.createCell(8).setCellValue(val8);
                valueList2.add(val7);
            }


            for (int j = 0; j < index; j++) {
                int length = valueList2.get(j).toString().getBytes().length * 256 + 200;

                if(maxWidth.get(j)!=null&&datarow.getCell(j)!=null){
                    //这里把宽度最大限制到15000
                    if (length > 15000) {
                        length = 15000;
                    }
                    maxWidth.put(j, Math.max(length, maxWidth.get(j)));//表格和标题选一个较大的作为列宽
                    datarow.getCell(j).setCellStyle(contentStyle);
                }else if(datarow.getCell(j)!=null){
                    maxWidth.put(j, length);//表格和标题选一个较大的作为列宽
                    datarow.getCell(j).setCellStyle(contentStyle);
                }

            }
        }

        //设置结尾行
        HSSFRow endRow = sheet.createRow(sheet.getLastRowNum() + 1);//第一行为标题行
        endRow.setHeightInPoints(23);//设置行高
        //设置头部样式
        CellStyle endStyle = workbook.createCellStyle();
        endStyle.setWrapText(true);//自动换行
        endStyle.setAlignment(CellStyle.ALIGN_LEFT);
        //设置字体，大小
        HSSFFont endfont = workbook.createFont();
        endfont.setFontName("黑体");
        endfont.setFontHeightInPoints((short) 10);//字体大小
        endfont.setColor(Font.COLOR_RED);
        endStyle.setFont(font);

        //记录每一行的长度
        List<Object> valueList3 = new ArrayList<>();
        endRow.createCell(1).setCellValue("填写人：");
        valueList3.add("填写人：");
        endRow.createCell(2).setCellValue(dto.getUserName());
        valueList3.add(dto.getUserName());
        endRow.createCell(6).setCellValue("审核人：");
        valueList3.add("审核人：");

        //根据更新人查询出库的经办人
        Manager manager = managerService.getById(dto.getUpdateUser());
        endRow.createCell(7).setCellValue(manager.getName());
        valueList3.add(manager.getName());

        //初始化标题的列宽，字体
        for (int i = 0,j=0; i < index; i++) {

            int length ;

            if(maxWidth.get(i)!=null&&endRow.getCell(i)!=null){
                length=valueList3.get(j++).toString().getBytes().length * 256 + 200;
                //这里把宽度最大限制到15000
                if (length > 15000) {
                    length = 15000;
                }
                maxWidth.put(i, Math.max(length, maxWidth.get(i)));//表格和标题选一个较大的作为列宽
                endRow.getCell(i).setCellStyle(endStyle);
            }else if(endRow.getCell(i)!=null){
                length=valueList3.get(j++).toString().getBytes().length * 256 + 200;
                maxWidth.put(i, length);//表格和标题选一个较大的作为列宽
                endRow.getCell(i).setCellStyle(endStyle);
            }

        }



        //设置列宽
        for (int i = 0; i < index; i++) {
            sheet.setColumnWidth(i, maxWidth.get(i));
        }

        String fileName = "出库材料列表.xls"; // 设置要导出的文件的名字
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition",
                "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
        response.flushBuffer();
        workbook.write(response.getOutputStream());
    }
}
