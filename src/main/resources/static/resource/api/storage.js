
//查询负责人接口
const getManagerList=()=>{
    return $axios({
        url:'/user/list',
        method:'get'
    })
}
//判断当前仓库名称是否重复
const isExistName=(name)=>{
    return $axios({
        url:`/storage/name/${name}`,
        method:'get'
    })
}

//添加仓库的接口
const addWarehouse = (params)=>{
    return $axios({
        url:'/storage',
        method:'post',
        data:{...params}
    })
}

//修改仓库接口
const editWarehouse =(params)=>{
    return $axios({
        url:'/storage',
        method:'put',
        data:{...params}
    })
}

//修改状态接口
const enableOrDisableManager =(params)=>{
    return $axios({
        url:'/storage',
        method:'put',
        data:{...params}
    })
}

//根据id查询单个仓库
function queryWarehouseById  (id){
    return $axios({
        url:`/storage/${id}`,
        method:'get'

    })
}


//分页查询所有
const getWarehouseList=(params)=>{
    return $axios({
        url:'/storage/page',
        method:'get',
        params
    })
}

//查询所有仓库列表
const getWarehouse=()=>{
    return $axios({
        url:'/storage/list',
        method:'get',

    })
}

//删除仓库
const deleteWarehouse=(ids)=>{
    return $axios({
        url:'/storage',
        method:'delete',
        params: {ids}
    })
}

//修改入库单并将材料入库
const addStorage=(params)=>{
    return $axios({
        url:'/storage/addStorage',
        method:'put',
        data:{...params}
    })
}

