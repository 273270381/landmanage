package com.suchness.landmanage.data.been

/**
 * @author: hejunfeng
 * @date: 2021/12/11 0011
 */
data class ResultInfo<T>( var pageNum : Int,
                          var pageSize : Int,
                          var totalPage : Int,
                          var total : Int,
                          var list : MutableList<T>)