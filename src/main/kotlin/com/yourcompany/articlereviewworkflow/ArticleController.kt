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

package com.yourcompany.articlereviewworkflow

import com.yourcompany.articlereviewworkflow.models.ArticleModel
import com.yourcompany.articlereviewworkflow.models.ArticleRequest
import com.yourcompany.articlereviewworkflow.models.TaskResource
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = ["*"])
class ArticleController {

  @Autowired
  private lateinit var assembler: ArticleAssembler

  @Autowired
  private lateinit var taskAssembler: ArticleTaskAssembler

  @Autowired
  private lateinit var service: ArticleService

  @GetMapping()
  fun getAll(): CollectionModel<ArticleModel> {
    return assembler.toCollectionModel(service.findAll())
  }

  @GetMapping("/{articleId}")
  fun getById(@PathVariable articleId: Long): ArticleModel {
    return service.findById(articleId).let(assembler::toModel)
  }

  @PostMapping
  fun createArticle(@RequestBody request: ArticleRequest): ArticleModel {
    return service.save(request.title, request.body).let(assembler::toModel)
  }

  @PutMapping("/{articleId}")
  fun updateArticle(
    @PathVariable articleId: Long,
    @RequestBody body: ArticleRequest?
  ): ArticleModel {
    require(body != null)
    return service.update(articleId, body.title, body.body).let(assembler::toModel)
  }

  @GetMapping("/{articleId}/tasks")
  fun getTasks(@PathVariable articleId: Long): TaskResource {
    val article = service.findById(articleId)
    return taskAssembler.toModel(article)
  }

  @PutMapping("/{articleId}/tasks/{task}")
  fun handleTask(@PathVariable articleId: Long, @PathVariable task: String): List<Link> {
    service.handleEvent(articleId, ArticleEvent.valueOf(task.uppercase()))
    return service.findById(articleId).let(assembler::toModel).links.toList()
  }

}