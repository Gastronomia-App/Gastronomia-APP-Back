package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductComponentResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Category;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:22-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Autowired
    private ProductComponentMapper productComponentMapper;

    @Override
    public ProductResponseDTO toDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        String categoryName = null;
        Long id = null;
        String name = null;
        String description = null;
        Double price = null;
        Double cost = null;
        Boolean controlStock = null;
        Integer stock = null;
        Boolean deleted = null;
        Boolean composite = null;
        List<ProductComponentResponseDTO> components = null;
        List<String> productGroups = null;

        categoryName = productCategoryName( product );
        id = product.getId();
        name = product.getName();
        description = product.getDescription();
        price = product.getPrice();
        cost = product.getCost();
        controlStock = product.isControlStock();
        stock = product.getStock();
        deleted = product.getDeleted();
        composite = product.isComposite();
        components = productComponentSetToProductComponentResponseDTOList( product.getComponents() );
        productGroups = map( product.getProductGroups() );

        ProductResponseDTO productResponseDTO = new ProductResponseDTO( id, name, description, price, cost, controlStock, stock, categoryName, deleted, composite, components, productGroups );

        return productResponseDTO;
    }

    @Override
    public List<ProductResponseDTO> toDTOList(List<Product> products) {
        if ( products == null ) {
            return null;
        }

        List<ProductResponseDTO> list = new ArrayList<ProductResponseDTO>( products.size() );
        for ( Product product : products ) {
            list.add( toDTO( product ) );
        }

        return list;
    }

    @Override
    public Product toEntity(ProductRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setName( dto.name() );
        product.setDescription( dto.description() );
        product.setPrice( dto.price() );
        product.setCost( dto.cost() );
        product.setStock( dto.stock() );
        if ( dto.controlStock() != null ) {
            product.setControlStock( dto.controlStock() );
        }

        return product;
    }

    @Override
    public Product updateProductFromDTO(Product product, ProductRequestDTO dto) {
        if ( dto == null ) {
            return product;
        }

        if ( dto.name() != null ) {
            product.setName( dto.name() );
        }
        if ( dto.description() != null ) {
            product.setDescription( dto.description() );
        }
        if ( dto.price() != null ) {
            product.setPrice( dto.price() );
        }
        if ( dto.cost() != null ) {
            product.setCost( dto.cost() );
        }
        if ( dto.stock() != null ) {
            product.setStock( dto.stock() );
        }
        if ( dto.controlStock() != null ) {
            product.setControlStock( dto.controlStock() );
        }

        return product;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected List<ProductComponentResponseDTO> productComponentSetToProductComponentResponseDTOList(Set<ProductComponent> set) {
        if ( set == null ) {
            return null;
        }

        List<ProductComponentResponseDTO> list = new ArrayList<ProductComponentResponseDTO>( set.size() );
        for ( ProductComponent productComponent : set ) {
            list.add( productComponentMapper.toDTO( productComponent ) );
        }

        return list;
    }
}
