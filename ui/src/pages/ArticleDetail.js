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

import {useContext, useEffect, useState} from "react";
import React from "react";
import {useParams} from "react-router-dom";
import {executeRequestFromLink, formatTaskName, hasTarget} from "../utils";
import {LoaderContext} from "../AppContext";
import {ARTICLES_ENDPOINT} from "../constants";
import defaultAxios from "../defaultAxios";

/**
 * Actions are to be a POST endpoint that starts with {baseUrl}/articles/{id}
 */
const getActionableLinks = (resource) => {
  const selfLink = resource._links.self.href;
  const templates = resource._templates;
  return Object.keys(templates)
      .filter((key) => key !== 'default') // ignore _templates with 'default' keys
      .filter((key) =>
          templates[key] && templates[key].method === 'POST' && templates[key].target.startsWith(selfLink)
      )
      .map((key) => ({
        name: key, // this is name of task that gets rendered in the button
        ...templates[key]
      }))
}

const ArticleDetail = (props) => {
  const {id} = useParams();
  const [article, setArticle] = useState(null);
  const [tasks, setTasks] = useState(null);
  const [_, setLoading] = useContext(LoaderContext);

  const fetchArticleDetail = async () => {
    try {
      setLoading(true);
      const response = await defaultAxios.get(ARTICLES_ENDPOINT + "/" + id)
      const data = response.data
      setArticle(data)
      const _tasks = getActionableLinks(data)
      setTasks(_tasks)
    } finally {
      setLoading(false);
    }
  }
  const handleTaskClick = async (task) => {
    setLoading(true)
    const response = await executeRequestFromLink(task)
    if (response.status === 200) await fetchArticleDetail()
    setLoading(false)
  }

  useEffect(() => {
    (async () => await fetchArticleDetail())()
  }, [])

  const handleSaveClick = async () => {
    setLoading(true);
    await executeRequestFromLink({
          ...article._templates.update,
          target: article._templates.update.target ?? article._links.self.href,
          data: article
        }
    )
    await fetchArticleDetail()
  }

  return (
      <div className={"mx-5 mt-3"}>
        <div className="d-flex flex-column" style={{height: "80vh"}}>
          {article && (<>
            <h4 className="fw-bolder">{article.title}</h4>

            <div>
                        <span className="fw-bold text-black-50 font-monospace">
                            <strong>{formatTaskName(article.state)}</strong>
                        </span>
              <span className="fw-bold text-black-50 font-monospace mx-2">|</span>
              <span className="fw-bold text-black-50 font-monospace">
                            <strong>{formatTaskName(article.reviewType)}</strong>
                        </span>
            </div>
            <span className="text-black-50">{article.updatedDate}</span>
            <div className="bg-primary dropdown-divider"/>
            <textarea
                className="form-control mt-3 flex-fill flex-grow-1"
                disabled={article?._templates?.update == null}
                onChange={e => setArticle({...article, body: e.target.value})}
                value={article.body}/>
          </>)
          }
          <div className="d-flex align-self-end justify-content-end">
            {
              article?._templates?.update && <button
                  className={"btn btn-outline-primary m-1 fw-bolder"}
                  onClick={() => handleSaveClick()}>Save</button>
            }
            {tasks && tasks.map(it =>
                <button
                    className={"btn btn-outline-primary m-1 fw-bolder"}
                    onClick={() => handleTaskClick(it)}>
                  {formatTaskName(it.name)}
                </button>
            )}

          </div>
        </div>
      </div>
  )
}

export default ArticleDetail;