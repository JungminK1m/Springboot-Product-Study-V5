package shop.mtcoding.productapp_v4.model.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import shop.mtcoding.productapp_v4.dto.orders.OrdersDto;
import shop.mtcoding.productapp_v4.model.orders.Orders;

@Mapper
public interface ProductRepository {

    public Product findById(Integer productId);

    public List<Product> findAll();

    public Product findByProductName(String productName);

    public void insert(Product product);

    public void update(Product product);

    public void deleteById(Integer productId);

    // 구매 시에 product QTY가 차감 되어야 함
    public void productQtyUpdate(OrdersDto ordersDto);

    // 구매 취소시 prouduct QTY 다시 증가
    public void productQtyReupdate(Orders orders);

}
