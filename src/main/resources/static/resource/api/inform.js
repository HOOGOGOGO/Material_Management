// 新增入库单接口
const addInform = (params) => {
    return $axios({
        url: '/inform',
        method: 'post',
        data: { ...params }
    })
}

//修改入库单接口
const editInform=(params)=>{
    return $axios({
        url:'/inform',
        method:'put',
        data:{...params}
    })
}


//根据入库单id查询完整入库单
const queryInformById = (id) => {
    return $axios({
        url: `/inform/${id}`,
        method: 'get',
    })
}



//获取当前用户所有的入库单
const getAllInform=(params)=>{
    return $axios({
        url:'/inform/page',
        method:'get',
        params
    })
}

//修改提交状态
const submitInform=(ids)=>{
    return $axios({
        url:`/inform/submit`,
        method:'put',
        params: {ids}
    })
}

const deleteInform=(id)=>{
    return $axios({
        url:`/inform/${id}`,
        method:'delete'
    })
}