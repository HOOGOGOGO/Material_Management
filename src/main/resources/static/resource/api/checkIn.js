//获取待审核的入库单
const getAllInformCheck=(params)=>{
    return $axios({
        url:'/checkIn/page',
        method:'get',
        params
    })
}
//单个审核入库单
const checkInform=(params)=>{
    return $axios({
        url:'/checkIn/inform',
        method:'put',
        data:{...params}
    })
}

//多个审核入库单
const checkInformMore=(ids)=>{
    return $axios({
        url:`/checkIn/inform/${ids}`,
        method:'put',
    })
}