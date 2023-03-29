package com.rblue.controller;


import ch.qos.logback.classic.jmx.MBeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.BaseContext;
import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.dto.MaterialDto;
import com.rblue.dto.OutformDto;
import com.rblue.entity.Inform;
import com.rblue.entity.InformMaterial;
import com.rblue.entity.Manager;
import com.rblue.entity.Material;
import com.rblue.service.InformMaterialService;
import com.rblue.service.InformService;
import com.rblue.service.ManagerService;
import com.rblue.service.MaterialService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/inform")
@RestController
@Slf4j
public class InformController {

    @Autowired
    private InformService informService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private InformMaterialService informMaterialService;

    @Autowired
    private MaterialService materialService;
    /**
     * 添加新的入库单
     *
     * @param inDto
     * @return
     */
    @PostMapping
    public R<String> saveInform(@RequestBody InformDto inDto) {

        if (inDto != null) {
            informService.saveInform(inDto);
        }
        return R.success("添加成功");

    }

    /**
     * 提交前，材料管理员更新入库材料单
     * @param informDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody InformDto informDto){
      informService.updateInform(informDto);
      return R.success("修改成功");
    }

    /**
     * 查询当前也得入库单信息
     * @param id
     * @param page
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param isSubmit
     * @param checkStatus
     * @param orderTime
     * @return
     */
    @GetMapping("/page")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public R<Page> getAllInform(Long id, int page, int pageSize , String beginTime,String endTime,String isSubmit,String checkStatus,String orderTime) {
        //创建分页构造
        Page<Inform> informPage = new Page<>(page, pageSize);
        Page<InformDto> dtoPage = new Page<>();
        //创建条件构造器
        LambdaQueryWrapper<Inform> lqw = new LambdaQueryWrapper<>();
        //入库单id是否存在
        lqw.like(id != null, Inform::getId, id);
        //时间段是否存在
        if(beginTime!=null&&endTime!=null){
            LocalDateTime bTime= LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime eTime= LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            lqw.gt(Inform::getCreateTime,bTime).lt(Inform::getCreateTime,eTime);
        }
        //判断是否为未提交
        lqw.eq(isSubmit!=null,Inform::getIsSubmit,0);
        //判断是否为审核失败
        lqw.eq(checkStatus!=null,Inform::getCheckStatus,2);

        //添加当前用户的id，未删除的、按照紧急程度排序
        Long loginId = BaseContext.getCurrentId();
        lqw.eq(Inform::getCreateUser, loginId).eq(Inform::getIsDeleted, 0);
        if(orderTime==null)
        lqw.orderByDesc(Inform::getUrgentStatus);
        else lqw.orderByDesc(Inform::getCreateTime);
        //得到查询结果
        informService.page(informPage, lqw);
        //复制
        BeanUtils.copyProperties(informPage, dtoPage, "records");
        //遍历查询结果获取创建人id并查看名字，插入到informDto中
        List<InformDto> dtoList = informPage.getRecords().stream().map(item -> {
            //创建dto对象
            InformDto dto = new InformDto();
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
        return R.success(dtoPage);

    }

    /**
     * 根据入库单号获取入库单全部信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<InformDto> selectById(@PathVariable Long id){
        InformDto dto=new InformDto();
       if(id!=null){
          dto= informService.selectById(id);
       }

        return R.success(dto);
    }

    @PutMapping("/submit")
    public R<String> updateSubmit(Long [] ids){
        //创建
        //获取当前所有需要提交的入库单号
       for(Long id:ids){
           //根据id查询当前入库单是否需要提交
           Inform inform=new Inform();
           //得到入库单记录
           inform =informService.getById(id);
           if(inform.getIsSubmit()==1){
               return R.error("当前入库单号："+id+"，已提交，请重新选择");
           }
           //获取当前入库单需要入库的全部材料
           //添加条件
           LambdaQueryWrapper<InformMaterial> lqw=new LambdaQueryWrapper<>();
           lqw.eq(InformMaterial::getInformId,id);
           List<InformMaterial> informMaterialList = informMaterialService.list(lqw);
         List<Material> materialList= informMaterialList.stream().map(item -> {
             //获取当前材料id
             Long materialId = item.getMaterialId();
             //查询出完整的材料信息
             Material material = new Material();
             //根据材料id查询出完整材料信息
             material=   materialService.getById(materialId);
             return material;
         }).collect(Collectors.toList());
           //判断是否存在材料是否已经提交
           for(Material material : materialList){
               if (material.getIsSubmit() == 1) {//当前材料已经提交审核，返回失败信息
                   return R.error("当前入库单号：" + id + "，需要入库的材料：" + material.getName() + "，已提交审核，请重新选择");
               }
           }
          //当前材料都未提交，修改提交信息
           for(Material material : materialList){
            material.setIsSubmit(1);
            //更新提交状态
            materialService.updateById(material);
           }
           //修改入库单提交状态
           inform.setIsSubmit(1);
           informService.updateById(inform);
       }
        return R.success("成功提交");
    }

    @DeleteMapping("/{id}") //参数占位符
    public R<String > deleteInform(@PathVariable Long id){
        if(id!=null){
            //删除入库单
            informService.removeById(id);
        }
        return R.success("删除成功");
    }

    /**
     * 下载单个入库单
     *
     * @param id
     * @return
     */
    @GetMapping("/export")
    public void downloadById(Long id, HttpServletResponse response) throws IOException {
        InformDto dto = new InformDto();
        if (id != null) {
            //根据id 查询信息
            dto = informService.selectById(id);
        }
        String informId = String.valueOf(dto.getId());

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
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 5);//合并第一行的0-5列
            sheet.addMergedRegion(cellRangeAddress);

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


            formRow.createCell(0).setCellValue("入库单");
        formRow.getCell(0).setCellStyle(formStyle);//将样式设置到表单行中

        //设置出库单信息行
        HSSFRow outformRow = sheet.createRow(1);//第一行为表单行
        outformRow.setHeightInPoints(30);

        //设置表单样式
        CellStyle informStyle = workbook.createCellStyle();
        informStyle.setWrapText(true);//自动换行
        informStyle.setAlignment(CellStyle.ALIGN_LEFT);//字体居中
        //特别处理时间格式
        CreationHelper createHelper = workbook.getCreationHelper();
        informStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyy-mm-dd hh:mm:ss"));

        //设置字体，大小
        HSSFFont informFont = workbook.createFont();
        informFont.setFontName("黑体");
        informFont.setFontHeightInPoints((short) 10);//字体大小
        informStyle.setFont(informFont);
        String formId = String.valueOf(dto.getId());
        outformRow.createCell(0).setCellValue("入库单号:");
        outformRow.createCell(1).setCellValue(formId);

        outformRow.createCell(3).setCellValue("填写时间：");
        Date date = Date.from(dto.getCreateTime().atZone(ZoneId.systemDefault()).toInstant());//将localDateTime转为date类型
        outformRow.createCell(4).setCellValue(date);
        //初始化出库单信息的样式
        for (int i = 0; i < 4; i++) {
            if(i!=2){
                maxWidth.put(i, outformRow.getCell(i).getStringCellValue().getBytes().length * 256 + 200);
                outformRow.getCell(i).setCellStyle(informStyle);//添加样式
            }
        }
        maxWidth.put(4, outformRow.getCell(4).getDateCellValue().toString().getBytes().length * 256 + 200);
        outformRow.getCell(4).setCellStyle(informStyle);//添加样式

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

        titleRow.createCell(4).setCellValue("存放仓库");
        valueList.add("存放仓库");

        titleRow.createCell(5).setCellValue("货架");
        valueList.add("货架");

        //初始化标题的列宽，字体
        for (int i = 0; i < 6; i++) {
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

            String val4 = materialDto.getWarehouse().getName();
            datarow.createCell(4).setCellValue(val4);
            valueList2.add(val4);

            String val5 = materialDto.getShelf();
            datarow.createCell(5).setCellValue(val5);
            valueList2.add(val5);



            for (int j = 0; j < 6; j++) {
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
        endRow.createCell(0).setCellValue("填写人：");
        valueList3.add("填写人：");

        endRow.createCell(1).setCellValue(dto.getUserName());
        valueList3.add(dto.getUserName());

        endRow.createCell(3).setCellValue("审核人：");
        valueList3.add("审核人：");

        //根据更新人查询出库的经办人
        Manager manager = managerService.getById(dto.getUpdateUser());
        endRow.createCell(4).setCellValue(manager.getName());
        valueList3.add(manager.getName());

        //初始化标题的列宽，字体
        for (int i = 0,j=0; i < 5; i++) {

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
        for (int i = 0; i < 6; i++) {
            sheet.setColumnWidth(i, maxWidth.get(i));
        }

        String fileName = "入库材料列表.xls"; // 设置要导出的文件的名字
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition",
                "attachment;filename=" + java.net.URLEncoder.encode(fileName, "8859_1"));
        response.flushBuffer();
        workbook.write(response.getOutputStream());

    }
}
