package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.entities.ArticleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : JpaRepository<ArticleEntity, Long> {

}