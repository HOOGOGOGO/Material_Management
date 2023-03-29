//按类别id查询材料

const outformMaterialListByCategoryId=(params)=>{
    return $axios({
        url:'/outform/categoryId',
        method:`get`,
        params
    })
}

//添加材料
const saveOutform=(params)=>{
    return $axios({
        url:'/outform/save',
        method:'post',
        data:{...params}
    })
}

//查询所有的出库单
const getAllOutform=(params)=>{
    return $axios({
        url:'/outform/page',
        method:'get',
        params
    })
}

//查询单个记录
const queryOutformById=(id)=>{
    return $axios({
        url:`/outform/${id}`,
        method:'get',
    })
}


//更新编辑后的记录
const editOutform=(params)=>{
    return $axios({
        url:"/outform",
        method:'put',
        data:{...params}
    })
}

//批量提交出库单
const submitOutform=(ids)=>{
    return $axios({
        url:`/outform/submit/${ids}`,
        method:'put'
    })
}

//删除出库单
const deleteOutform=(id)=>{
    return $axios({
        url:`/outform/${id}`,
        method:'delete'
    })
}

//仓库管理员修改出库状态
const outWarehouse=(id)=>{
    return $axios({
        url:`/outform/outStatus/${id}`,
        method:'put'
    })
}

//材料退库
const backStorage=(params)=>{
    return $axios({
        url:`/outform/back`,
        method:'put',
        data:{...params}
    })

}

//下载功能
const exportFile=(id)=>{
    return $axios({
        url:`/outform/export/${id}`,
        method:'get',
        responseType: "blob"
    })
}

//查询所有的出库单做统计
const getAllOutFormStatistics=(params)=>{
    return $axios({
        url:`/outform/statistic`,
        method:'get',
        params
    })
}
