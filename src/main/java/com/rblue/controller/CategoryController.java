package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.R;
import com.rblue.entity.Category;
import com.rblue.entity.Material;
import com.rblue.service.CategoryService;
import com.rblue.service.MaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MaterialService materialService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加材料分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody Category category){
        //添加分类信息
        if(category!=null){
            //判断当前材料名称是否重复
            LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
            lqw.eq(Category::getName,category.getName()).eq(Category::getIsDeleted,0);
            List<Category> list = categoryService.list(lqw);
            if(list!=null&&list.size()>0){//当前材料名称已经存在
                return R.error(category.getName()+"已存在");
            }else{
                categoryService.save(category);
                //删除redis缓存
                Set keys=redisTemplate.keys("category_*");
                redisTemplate.delete(keys);
            }

        }
        return R.success("添加成功");
    }

    /**
     * 查询所有
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectAll(int page, int pageSize,String name){
        //创建分页构造器
        Page<Category> categoryPage=new Page<>(page,pageSize);
        //先从redis中获取缓存数据
        //设置当前页和页大小构成key
        String category_key;
        String total_key;
        if (name == null) {
            category_key="category_"+page+"_"+pageSize;
            total_key="category_total_"+page+"_"+pageSize;
        }else{
            category_key="category_"+page+"_"+pageSize+"_"+name;
            total_key="category_total_"+page+"_"+pageSize+"_"+name;
        }

        Long total;
        //查询redis中是否有分类信息
      List<Category> categories= (List<Category>) redisTemplate.opsForValue().get(category_key);
      total= (Long) redisTemplate.opsForValue().get(total_key);
      if(categories!=null && total!=null){
          categoryPage.setRecords(categories);
          categoryPage.setTotal( total);
      }else{
          //创建构造器
          LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
          //添加查询条件
          lqw.like(name!=null,Category::getName,name);
          lqw.eq(Category::getIsDeleted,0);
          //添加排序条件,按更新时间升序
          lqw.orderByDesc(Category::getUpdateTime);

          //查询数据库
          categoryService.page(categoryPage,lqw);
          //将记录插入redis中
          if(name==null){
              redisTemplate.opsForValue().set(category_key,categoryPage.getRecords(),60, TimeUnit.MINUTES);//60分钟
              redisTemplate.opsForValue().set(total_key,categoryPage.getTotal(),60,TimeUnit.MINUTES);
          }else{
              redisTemplate.opsForValue().set(category_key,categoryPage.getRecords(),5, TimeUnit.MINUTES);//60分钟
              redisTemplate.opsForValue().set(total_key,categoryPage.getTotal(),5,TimeUnit.MINUTES);
          }

        }
        return R.success(categoryPage);
    }

    /**
     * 根据id修改
     * @param category
     * @return
     */
    @PutMapping
    public R<String> EditCategory(@RequestBody Category category){
        //根据id修改
        if(category!=null)
        categoryService.updateById(category);
        //删除redis缓存
        Set keys=redisTemplate.keys("category_*");
        redisTemplate.delete(keys);
       keys=redisTemplate.keys("material_*");
        redisTemplate.delete(keys);//删除redis的指定key
        return R.success("修改成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        //遍历ids单个修改is——deleted为1
        for (Long id:ids){
            if(id!=null){
                //判断此时是否有关联了材料
                LambdaQueryWrapper<Material> lqw=new LambdaQueryWrapper<>();
                lqw.eq(Material::getCategoryId,id).eq(Material::getIsDeleted,0);
                List<Material> list = materialService.list(lqw);
                if(list!=null&&list.size()>0){//当前分类关联了材料
                    return R.error("不允许删除，当前分类下还有材料");
                }else{//当前分类未关联材料,可以删除
                    Category category=new Category();
                    category.setId(id);
                    category.setIsDeleted(1);
                    categoryService.updateById(category);
                    //删除redis缓存
                    Set keys=redisTemplate.keys("category_*");
                    redisTemplate.delete(keys);
                }

            }
        }
        log.info(String.valueOf(ids));
        return R.success("删除成功");
    }

    /**
     * 查询所有材料分类
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> lists(){
        //创建构造器
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        //添加查询条件
        lqw.eq(Category::getIsDeleted,0);
        List<Category> lists=categoryService.list(lqw);
        return R.success(lists);
    }
}
