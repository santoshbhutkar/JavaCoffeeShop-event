package org.workshop.coffee.repository;

import org.workshop.coffee.domain.Product;
import org.workshop.coffee.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Serializable> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

}

