function getMemberList (params) {
  return $axios({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 修改---启用禁用接口
function enableOrDisableManager (params) {
  return $axios({
    url: '/user',
    method: 'put',
    data: { ...params }
  })
}

// 新增---添加员工
function addManager (params) {
  return $axios({
    url: '/user',
    method: 'post',
    data: { ...params }
  })
}

// 修改---添加员工
function editManager (params) {
  return $axios({
    url: '/user',
    method: 'put',
    data: { ...params }
  })
}
//删除员工
const deleteUser = (ids)=>{
  return $axios({
    url:'/user',
    method:'delete',
    params: {ids}
  })
}
// 修改页面反查详情接口
function queryEmployeeById (id) {
  return $axios({
    url: `/user/${id}`,
    method: 'get'
  })
}
