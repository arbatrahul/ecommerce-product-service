package org.example.product.consumer;

import org.example.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CartEventConsumer {

    @Autowired
    private ProductService productService;

    @KafkaListener(topics = "cart-events", groupId = "product-service-group")
    public void handleCartEvent(@Payload Map<String, Object> cartEvent, 
                               @Header(KafkaHeaders.KEY) String eventType) {
        
        try {
            Long userId = Long.valueOf(cartEvent.get("userId").toString());
            String eventTypeValue = cartEvent.get("eventType").toString();
            
            switch (eventTypeValue) {
                case "ITEM_ADDED":
                    handleItemAdded(cartEvent);
                    break;
                case "ITEM_UPDATED":
                    handleItemUpdated(cartEvent);
                    break;
                case "ITEM_REMOVED":
                    handleItemRemoved(cartEvent);
                    break;
                case "CHECKOUT_INITIATED":
                    handleCheckoutInitiated(cartEvent);
                    break;
                default:
                    System.out.println("Unknown cart event type: " + eventTypeValue);
            }
        } catch (Exception e) {
            System.err.println("Error processing cart event: " + e.getMessage());
        }
    }

    private void handleItemAdded(Map<String, Object> event) {
        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());
        
        // Reserve stock for cart item
        boolean stockReserved = productService.updateStock(productId, quantity);
        
        if (!stockReserved) {
            System.err.println("Failed to reserve stock for product: " + productId + ", quantity: " + quantity);
            // In a real system, you might want to send a compensation event
        } else {
            System.out.println("Stock reserved for product: " + productId + ", quantity: " + quantity);
        }
    }

    private void handleItemUpdated(Map<String, Object> event) {
        // For item updates, we might need to adjust stock reservations
        // This is a simplified implementation
        System.out.println("Cart item updated: " + event);
    }

    private void handleItemRemoved(Map<String, Object> event) {
        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());
        
        // Restore stock when item is removed from cart
        productService.restoreStock(productId, quantity);
        System.out.println("Stock restored for product: " + productId + ", quantity: " + quantity);
    }

    private void handleCheckoutInitiated(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        Integer totalItems = Integer.valueOf(event.get("quantity").toString());
        
        System.out.println("Checkout initiated for user: " + userId + ", total items: " + totalItems);
        // Additional processing can be added here
    }
}
