package com.example.demo.service.order.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.example.demo.base.response.BaseResponse;
import com.example.demo.dao.OrderProductRepository;
import com.example.demo.dao.OrderRepository;
import com.example.demo.dao.ProductRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.dto.cart.CartDTO;
import com.example.demo.dto.cart.CartProductDTO;
import com.example.demo.dto.order.OrderDTO;
import com.example.demo.dto.order.OrderProductDTO;
import com.example.demo.dto.product.ProductDTO;
import com.example.demo.entity.CustomUserDetails;
import com.example.demo.entity.order.Order;
import com.example.demo.entity.order.OrderProduct;
import com.example.demo.entity.order.OrderProductID;
import com.example.demo.entity.order.Product;
import com.example.demo.entity.user.User;
import com.example.demo.service.order.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public BaseResponse placeOrder(List<CartProductDTO> cartDto) {
        try {

            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();

            User user = userRepository.getById(userDetails.getUser().getId());

            Order order = new Order();
            order.setCreatedAt(new Date());
            order.setUser(user);
            orderRepository.save(order);
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setCreatedAt(order.getCreatedAt());
            orderDTO.setProducts(new ArrayList<>());

            for (CartProductDTO cartProductDto : cartDto) {
                OrderProductID odid = new OrderProductID(order.getId(), cartProductDto.getId());
                Product product = productRepository.getById(cartProductDto.getId());
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setQuanity(cartProductDto.getQuantity());
                orderProduct.setId(odid);
                orderProduct.setOrder(order);
                orderProduct.setProduct(product);
                orderProductRepository.save(orderProduct);

                // Return value
                OrderProductDTO productDTO = OrderProductDTO.builder()
                        .name(cartProductDto.getName())
                        .price(cartProductDto.getPrice())
                        .quantity(cartProductDto.getQuantity())
                        .build();
                orderDTO.getProducts().add(productDTO);

            }

            return new BaseResponse<>(HttpStatus.OK, "Place success!", orderDTO);
        } catch (Exception e) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Place failed! " + e.getMessage());
        }
    }

    @Override
    public BaseResponse getOrderInfo() {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();

            User user = userRepository.getById(userDetails.getUser().getId());
            Collection<Order> orders = user.getOrders();
            List<OrderDTO> orderDTOs = new ArrayList<>();
            for (Order order : orders) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setCreatedAt(order.getCreatedAt());
                orderDTO.setProducts(new ArrayList<>());

                Collection<OrderProduct> products=order.getProducts();
                for(OrderProduct orderProduct:products)
                {
                     OrderProductDTO productDTO = OrderProductDTO.builder()
                        .name(orderProduct.getProduct().getName())
                        .price(orderProduct.getProduct().getPrice())
                        .quantity(orderProduct.getQuanity())
                        .build();
                        orderDTO.getProducts().add(productDTO);
                }
                orderDTOs.add(orderDTO);
            }
            return new BaseResponse<>(HttpStatus.OK, "All order", orderDTOs);
        } catch (Exception e) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Get orders failed!" + e.getMessage());
        }
    }

}
