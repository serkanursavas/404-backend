package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

}
