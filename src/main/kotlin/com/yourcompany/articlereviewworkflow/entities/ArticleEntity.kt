/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yourcompany.articlereviewworkflow.entities

import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState
import com.yourcompany.articlereviewworkflow.statemachine.articles.ReviewType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "tbl_article")
class ArticleEntity {

  @field:Id
  @field:GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long? = null

  @field:Column
  @field:Enumerated(EnumType.STRING)
  var state: ArticleState = ArticleState.DRAFT

  @field:Column(nullable = false)
  var title: String = ""

  @field:Column(length = 5000)
  var body: String = ""

  @field:UpdateTimestamp
  lateinit var updatedDate: LocalDateTime
    private set

  @field:CreationTimestamp
  lateinit var createdDate: LocalDateTime
    private set

  @field:Column(nullable = false)
  @field:Enumerated(EnumType.STRING)
  lateinit var reviewType: ReviewType
    private set


  @field:ElementCollection(fetch = FetchType.EAGER)
  @field:Enumerated(value = EnumType.STRING)
  @field:OrderColumn
  private val events = mutableListOf<ArticleEvent>()

  fun getPastEvents(): List<ArticleEvent> {
    return events.toList()
  }

  fun consumeEvent(event: ArticleEvent) {
    events.add(event)
  }

  fun isPublished() = state == ArticleState.PUBLISHED

  companion object {
    fun create(title: String, body: String, reviewType: ReviewType): ArticleEntity {
      require(title.isNotBlank())
      return ArticleEntity().apply {
        this.title = title
        this.body = body
        this.reviewType = reviewType
      }
    }
  }
}