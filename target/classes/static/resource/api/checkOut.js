//获取待审核的入库单
const getAllOutformCheck=(params)=>{
    return $axios({
        url:'/checkOut/page',
        method:'get',
        params
    })
}

//单个审核出库单
const checkOutform=(params)=>{
    return $axios({
        url:'/checkOut/outform',
        method:'put',
        data:{...params}
    })
}

//多个审核出库单
const checkOutformMore=(ids)=>{
    return $axios({
        url:`/checkOut/outform/${ids}`,
        method:'put',
    })
}