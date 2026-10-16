package com.ikobana.backend.config;

import com.ikobana.backend.dto.ProductResponse;
import com.ikobana.backend.dto.TransactionItemResponse;
import com.ikobana.backend.dto.TransactionResponse;
import com.ikobana.backend.dto.UserDTO;
import com.ikobana.backend.entity.DeliveryStatus;
import com.ikobana.backend.entity.PaymentMethod;
import com.ikobana.backend.entity.PaymentStatus;
import com.ikobana.backend.entity.Product;
import com.ikobana.backend.entity.ProductCategory;
import com.ikobana.backend.entity.Transaction;
import com.ikobana.backend.entity.TransactionItem;
import com.ikobana.backend.entity.User;
import com.ikobana.backend.entity.UserRole;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration class for ModelMapper with custom Entity <-> DTO type mappings.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Set global matching strategy
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);

        // Converter for User.role (String) -> UserDTO.role (UserRole)
        Converter<String, UserRole> userRoleConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            try {
                return UserRole.valueOf(ctx.getSource());
            } catch (IllegalArgumentException e) {
                return UserRole.CUSTOMER;
            }
        };

        // Custom mapping: User -> UserDTO (excludes passwordHash for security)
        modelMapper.createTypeMap(User.class, UserDTO.class)
                .addMappings(mapper -> {
                    mapper.map(User::getId, UserDTO::setId);
                    mapper.map(User::getEmail, UserDTO::setEmail);
                    mapper.map(User::getUsername, UserDTO::setUsername);
                    mapper.map(User::getFullName, UserDTO::setFullName);
                    mapper.map(User::getPhone, UserDTO::setPhone);
                    mapper.using(userRoleConverter).map(User::getRole, UserDTO::setRole);
                    mapper.map(User::getIsActive, UserDTO::setIsActive);
                    mapper.map(User::getProfilePictureUrl, UserDTO::setProfilePictureUrl);
                    mapper.map(User::getCreatedAt, UserDTO::setCreatedAt);
                    mapper.map(User::getUpdatedAt, UserDTO::setUpdatedAt);
                });

        // Converter for Product.category (String) -> ProductResponse.category (ProductCategory)
        Converter<String, ProductCategory> productCategoryConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            try {
                return ProductCategory.valueOf(ctx.getSource());
            } catch (IllegalArgumentException e) {
                return ProductCategory.LAINNYA;
            }
        };

        // Custom mapping: Product -> ProductResponse (with computed fields)
        modelMapper.createTypeMap(Product.class, ProductResponse.class)
                .addMappings(mapper -> {
                    mapper.using(productCategoryConverter).map(Product::getCategory, ProductResponse::setCategory);
                    mapper.map(Product::isLowStock, ProductResponse::setIsLowStock);
                    mapper.map(Product::calculateMargin, ProductResponse::setMargin);
                });

        // Custom mapping: TransactionItem -> TransactionItemResponse
        modelMapper.createTypeMap(TransactionItem.class, TransactionItemResponse.class)
                .addMappings(mapper -> {
                    mapper.map(TransactionItem::getId, TransactionItemResponse::setId);
                    mapper.map(TransactionItem::getProductId, TransactionItemResponse::setProductId);
                    mapper.map(TransactionItem::getProductName, TransactionItemResponse::setProductName);
                    mapper.map(TransactionItem::getQuantity, TransactionItemResponse::setQuantity);
                    mapper.map(TransactionItem::getPricePerUnit, TransactionItemResponse::setPricePerUnit);
                    mapper.map(TransactionItem::getSubtotal, TransactionItemResponse::setSubtotal);
                });

        // Converter for nested List<TransactionItem> -> List<TransactionItemResponse>
        Converter<List<TransactionItem>, List<TransactionItemResponse>> itemsConverter = ctx -> {
            if (ctx.getSource() == null) return Collections.emptyList();
            return ctx.getSource().stream()
                    .map(item -> modelMapper.map(item, TransactionItemResponse.class))
                    .collect(Collectors.toList());
        };

        // Enums converters for Transaction
        Converter<String, PaymentMethod> paymentMethodConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            try { return PaymentMethod.valueOf(ctx.getSource()); } catch (IllegalArgumentException e) { return null; }
        };
        Converter<String, PaymentStatus> paymentStatusConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            try { return PaymentStatus.valueOf(ctx.getSource()); } catch (IllegalArgumentException e) { return null; }
        };
        Converter<String, DeliveryStatus> deliveryStatusConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            try { return DeliveryStatus.valueOf(ctx.getSource()); } catch (IllegalArgumentException e) { return null; }
        };

        // Custom mapping: Transaction -> TransactionResponse
        modelMapper.createTypeMap(Transaction.class, TransactionResponse.class)
                .addMappings(mapper -> {
                    mapper.map(Transaction::getId, TransactionResponse::setId);
                    mapper.map(Transaction::getTransactionId, TransactionResponse::setTransactionId);
                    mapper.map(Transaction::getUserId, TransactionResponse::setUserId);
                    mapper.map(Transaction::getCustomerName, TransactionResponse::setCustomerName);
                    mapper.map(Transaction::getCustomerPhone, TransactionResponse::setCustomerPhone);
                    mapper.map(Transaction::getCustomerEmail, TransactionResponse::setCustomerEmail);
                    mapper.using(itemsConverter).map(Transaction::getItems, TransactionResponse::setItems);
                    mapper.map(Transaction::getTotalAmount, TransactionResponse::setTotalAmount);
                    mapper.using(paymentMethodConverter).map(Transaction::getPaymentMethod, TransactionResponse::setPaymentMethod);
                    mapper.using(paymentStatusConverter).map(Transaction::getPaymentStatus, TransactionResponse::setPaymentStatus);
                    mapper.using(deliveryStatusConverter).map(Transaction::getDeliveryStatus, TransactionResponse::setDeliveryStatus);
                    mapper.map(Transaction::getNotes, TransactionResponse::setNotes);
                    mapper.map(Transaction::getCreatedAt, TransactionResponse::setCreatedAt);
                    mapper.map(Transaction::getUpdatedAt, TransactionResponse::setUpdatedAt);
                });

        return modelMapper;
    }
}
