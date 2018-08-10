package com.example.demo.dao;

import com.example.demo.bean.Goods;
import com.example.demo.bean.GoodsPhoto;
import com.example.demo.valuable.GoodsMoreValuable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoodsDao extends JpaRepository<Goods,Long> {
        @Query("select p from Goods p where p.goods_store_id= ?1")
        List<Goods> findByGoods_store_id(Long goods_store_id);
//        @Query("select new GoodsMoreValuable(u,a) from Goods g,GoodsPhotoDao p,Accessory a where g.id=?1 and g.id=p.goods_id and p.photo_id=a.id")
        @Query(value = "select * from wemall_goods  where goods_name like ?1 and goods_store_id=?2", nativeQuery = true)
        List<Goods> findByGoodsNameLike(String goodsName,Long goods_store_id);
        @Query("select p.goods_inventory from Goods p where p.id=?1")
        Integer findGoods_invertoryById(Long id);

        @Query("select p from Goods p where p.goods_store_id=?1 and p.goods_name like %?2% ")
        List<Goods> findByGoods_store_idAndGoods_nameIsLike(Long good_store_id,String goods_name);

}
