
//获取所有的材料分类
const getMaterialList=()=>{
    return $axios({
        url:'/category/list',
        method:'get'
    })
}

//获取所有的材料
const getAllMaterial=(params)=>{
    return $axios({
        url:'/material/page',
        method:'get',
        params
    })
}


//添加材料的接口
const addMaterial = (params)=>{
    return $axios({
        url:'/material',
        method:'post',
        data:{...params}
    })
}

//修改保存
const editMaterial = (params) =>{
    return $axios({
        url:`/material`,
        method:'put',
        data:{...params}
    })
}

//根据id查询单个材料
const queryMaterialById = (id)=>{
    return $axios({
        url:`/material/${id}`,
        method:'get'
    })
}

//更改状态
const enableOrDisableMaterial=(params)=>{
    return $axios({
        url:`/material`,
        method: 'put',
        data:{...params}
    })
}

//删除材料
const deleteMaterial=(ids)=>{
    return $axios({
        url:'/material',
        method:'delete',
        params:{ids}
    })
}

//只显示未入库材料
const onlyShow =(params)=>{
    return $axios({
        url:'/material/only',
        method:'get',
        params
    })
}

//根据分类id获取材料
const queryMaterialListByCategoryId=(params)=>{
    return $axios({
        url:'/material/categoryId',
        method:`get`,
        params
    })
}

//按名称查询材料
const queryMaterialListByName=(params)=>{
    return $axios({
        url:`/material/name`,
        method:`get`,
        params
    })
}