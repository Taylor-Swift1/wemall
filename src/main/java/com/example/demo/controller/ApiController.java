package com.example.demo.controller;


import com.example.demo.bean.*;
import com.example.demo.dao.*;
import com.example.demo.valuable.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/")
@CrossOrigin
public class ApiController {
    @Autowired
    GoodsDao goodsDao;

    @Autowired
    GoodsPhotoDao goodsPhotoDao;

    @Autowired
    AccessoryDao accessoryDao;

    @Autowired
    EvaluateDao evaluateDao;

    @Autowired
    UserDao userDao;

    @Autowired
    StoreCartDao storeCartDao;

    @Autowired
    GoodsCartDao goodsCartDao;

    @Autowired
    OrderFormDao orderFormDao;

    @GetMapping("/goods") //商品 id 名字 主图 价格
    public List<GoodsValuable> find(@RequestParam Long goods_store_id){
        List<Goods> goods=goodsDao.findByGoods_store_id(goods_store_id);
        List<GoodsValuable> list=new ArrayList<>();
        for(Goods goods1:goods){
            Accessory accessory=accessoryDao.findById(goods1.getGoods_main_photo_id()).orElse(new Accessory());
            GoodsValuable goodsValuable=new GoodsValuable(goods1.getId(),goods1.getGoods_name(),new ZuTu(accessory.getPath(),accessory.getName(),accessory.getExt()),goods1.getGoods_price());
            list.add(goodsValuable);
        }

        return list;
    }
   @GetMapping("/goods/id")
    public GoodsMoreValuable find2(@RequestParam Long id){
        List<ZuTu> zuTus=new ArrayList<>();
        Goods goods=goodsDao.findById(id).orElse(new Goods());
        List<Long> list=goodsPhotoDao.findPhoto_idByGoods_id(id);
        for(Long l:list){
            Accessory accessory=accessoryDao.findById(l).orElse(new Accessory());
            zuTus.add(new ZuTu(accessory.getPath(),accessory.getName(),accessory.getExt()));
        }
       GoodsMoreValuable goodsMoreValuable=new GoodsMoreValuable(id,goods.getGoods_name(),zuTus,goods.getGoods_price(),goods.getGoods_property(),goods.getGoods_details());

        return goodsMoreValuable;
   }
   @GetMapping("/goods/comment/id")
    public EvaluableValuable find3(@RequestParam Long id){
        List<CommentValuable> commentValuables=new ArrayList<>();
        List<Evaluate> evaluates=evaluateDao.findByEvaluate_goods_id(id);
        for(Evaluate evaluate:evaluates){
            commentValuables.add(new CommentValuable(evaluate.getAddtime(),userDao.findById(evaluate.getEvaluate_user_id()).orElse(new User()).getUsername(),evaluate.getEvaluate_info()));
        }
        return new EvaluableValuable(commentValuables.size(),commentValuables);
   }
   @GetMapping("/cart")
    public List<CartValuable> find4(@RequestParam Long user_id){
        List<CartValuable> list=new ArrayList<>();
        StoreCart storeCart=storeCartDao.findByUser_id(user_id);
        List<GoodsCart> goodsCarts=goodsCartDao.findBySc_id(storeCart.getId());
        for(GoodsCart goodsCart:goodsCarts){
            Goods goods=goodsDao.findById(goodsCart.getGoods_id()).orElse(new Goods());
            Accessory accessory=accessoryDao.findById(goods.getGoods_main_photo_id()).orElse(new Accessory());
            list.add(new CartValuable(goods.getId(),new ZuTu(accessory.getPath(),accessory.getName(),accessory.getExt()),goods.getGoods_name(),goods.getGoods_price(),goods.getGoods_inventory(),goodsCart.getCount()));
        }
        return list;
   }
   @GetMapping("/addToCart")
    public void addToCart(@RequestParam Long user_id,@RequestParam Long goods_id,@RequestParam String spec_info){
        StoreCart storeCart=storeCartDao.findByUser_id(user_id);
        Goods goods=goodsDao.findById(goods_id).orElse(new Goods());
        try{
            GoodsCart goodsCart=goodsCartDao.findByGoods_idAndSc_idAndSpec_info(goods_id,storeCart.getId(),spec_info);
            goodsCart.setCount(goodsCart.getCount()+1);
            goodsCartDao.save(goodsCart);
            storeCart.setTotal_price(storeCart.getTotal_price()+goods.getGoods_price());
            storeCartDao.save(storeCart);
        }catch (Exception e){
            GoodsCart goodsCart=new GoodsCart();
            Date date=new Date();
            goodsCart.setAddtime(date);
            goodsCart.setDeletestatus(false);
            goodsCart.setCount(1);
            goodsCart.setPrice(goods.getGoods_price());
            goodsCart.setSpec_info(spec_info);
            goodsCart.setGoods_id(goods_id);
            goodsCart.setSc_id(storeCart.getId());
            goodsCartDao.save(goodsCart);
            storeCart.setTotal_price(storeCart.getTotal_price()+goods.getGoods_price());
            storeCartDao.save(storeCart);
        }

   }
   @GetMapping("/submit")
    public int submit(@RequestParam Long user_id,@RequestParam Long addr_id){
        StoreCart storeCart=storeCartDao.findByUser_id(user_id);
        List<GoodsCart> goodsCarts=goodsCartDao.findBySc_id(storeCart.getId());
        for(GoodsCart goodsCart:goodsCarts){
            if(goodsDao.findGoods_invertoryById(goodsCart.getGoods_id())<goodsCart.getCount()){
                return 0;
            }

        }
        for(GoodsCart goodsCart:goodsCarts){
            Goods goods=goodsDao.findById(goodsCart.getGoods_id()).orElse(new Goods());
            goods.setGoods_inventory(goods.getGoods_inventory()-goodsCart.getCount());
            goodsDao.save(goods);
            goodsCart.setSc_id(null);
            goodsCartDao.save(goodsCart);
        }
        OrderForm orderForm=new OrderForm();
        Date date=new Date();
        orderForm.setAddtime(date);
        orderForm.setDeletestatus(0);
        orderForm.setGoods_amount(storeCart.getTotal_price());
        orderForm.setInvoicetype(0);
        orderForm.setOrder_id("1"+date.getYear()+date.getMonth()+date.getDay()+date.getHours()+date.getMinutes()+date.getSeconds());
        orderForm.setOrder_status(10);
        orderForm.setShip_price(0.0);
        orderForm.setTotalprice(orderForm.getShip_price()+orderForm.getGoods_amount());
        orderForm.setAddr_id(addr_id);
        orderForm.setStore_id(storeCart.getStore_id());
        orderForm.setUser_id(user_id);
        orderForm.setAuto_confirm_email(0);
        orderForm.setAuto_confirm_sms(0);
        orderFormDao.save(orderForm);
        storeCart.setTotal_price(0.0);
        storeCartDao.save(storeCart);
        return 1;
   }
   @GetMapping("/delAndAdd")
    public int delAndAdd(@RequestParam Long user_id,@RequestParam Long goods_id,@RequestParam Integer status){
            StoreCart storeCart=storeCartDao.findByUser_id(user_id);
            GoodsCart goodsCart=goodsCartDao.findByGoods_idAndSc_id(goods_id,storeCart.getId());
            Integer goods_inventory=goodsDao.findGoods_invertoryById(goods_id);
            if(status==0){
                goodsCart.setCount(goodsCart.getCount()-1);
                goodsCartDao.save(goodsCart);
                return 1;
            }
            else {
                if(goods_inventory>goodsCart.getCount()){
                    goodsCart.setCount(goodsCart.getCount()+1);
                    goodsCartDao.save(goodsCart);
                    return 1;
                }else {
                    return 0;
                }
            }
   }
    @GetMapping("/goods/like") //商品 id 名字 主图 价格
    public List<GoodsValuable> findLike(@RequestParam Long goods_store_id,@RequestParam String word){
        List<Goods> goods=goodsDao.findByGoods_store_idAndGoods_nameIsLike(goods_store_id,word);
        List<GoodsValuable> list=new ArrayList<>();
        for(Goods goods1:goods){
            Accessory accessory=accessoryDao.findById(goods1.getGoods_main_photo_id()).orElse(new Accessory());
            GoodsValuable goodsValuable=new GoodsValuable(goods1.getId(),goods1.getGoods_name(),new ZuTu(accessory.getPath(),accessory.getName(),accessory.getExt()),goods1.getGoods_price());
            list.add(goodsValuable);
        }

        return list;
    }





}
