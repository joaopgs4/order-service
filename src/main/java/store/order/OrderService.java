package store.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import store.product.ProductController;
import store.product.ProductOut;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductController productController;

    public Order create(Order order) {
        order.date(new Date());
        double totalOrder = 0.0;
        List<OrderItem> newOrderItems = new ArrayList<>();
    
        for (OrderItem oi : order.items()) {
            ResponseEntity<ProductOut> response = productController.findById(oi.product().id());
            ProductOut product = response.getBody();
    
            if (product == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product with id=" + oi.product().id() + " does not exist.");
            }
    
            double itemTotal = product.price() * oi.quantity();
            totalOrder += itemTotal;
    
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(oi.quantity())
                    .total(itemTotal)
                    .build();
    
            newOrderItems.add(orderItem);
        }
    
        order.total(totalOrder);
        final Order savedOrder = orderRepository.save(new OrderModel(order)).to();
        savedOrder.items(new ArrayList<>());
    
        newOrderItems.forEach(orderItem -> {
            orderItem.order(savedOrder);
            savedOrder.items().add(orderItemRepository.save(new OrderItemModel(orderItem)).to());
        });
    
        return savedOrder;
    }
    

    public Order findById(String id, String idAccount) {
        Order found = orderRepository.findByIdAndIdAccount(id, idAccount)
                .map(OrderModel::to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        found.items(orderItemRepository.findByIdOrder(id)
                .stream()
                .map(OrderItemModel::to)
                .toList());

        return found;
    }

    public List<Order> findAll() {
        return StreamSupport
                .stream(orderRepository.findAll().spliterator(), false)
                .map(o -> {
                    Order order = o.to();
                    order.items(orderItemRepository.findByIdOrder(order.id())
                            .stream()
                            .map(OrderItemModel::to)
                            .toList());
                    return order;
                })
                .toList();
    }
}