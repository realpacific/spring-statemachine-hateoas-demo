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
import {Link} from "react-router-dom";
import {formatTaskName} from "../utils";
import {LoaderContext} from "../AppContext";
import {ARTICLES_ENDPOINT} from "../constants";
import defaultAxios from "../defaultAxios";

const ArticleList = (props) => {
    const [articles, setArticles] = useState([])
    const [_, setIsLoading] = useContext(LoaderContext)
    const [title, setTitle] = useState(undefined)

    const fetchArticles = async () => {
        setIsLoading(true)
        const response = await defaultAxios.get(ARTICLES_ENDPOINT)
        if (response.status === 200) {
            setArticles(response.data?._embedded?.articles ?? [])
        }
        setIsLoading(false)
    };

    useEffect(() => {
        (async () => await fetchArticles())()
    }, [])


    const postArticle = async () => {
        setIsLoading(true)
        const response = await defaultAxios.post(ARTICLES_ENDPOINT, {title, body: ""})
        if (response.status === 200) {
            await fetchArticles()
        }
        setIsLoading(false)
    }

    return (
        <>
            <div className="row">
                {
                    articles.map(it => (
                        <div className="col-lg-12">
                            <div className="card h-100">
                                <div className="card-body">
                                    <h5 className="card-title fw-bolder">{it.title}</h5>
                                    <span className="card-subtitle opacity-75 fw-bold">
                                        {formatTaskName(it.state)}
                                    </span>
                                    <div className="card-text">
                                        <p className="text-truncate fw-light"> {it.body}</p>
                                    </div>

                                    <Link to={`/${it.id}`} className="text-decoration-none">
                                        <a className="card-link">View</a>
                                    </Link>
                                </div>
                            </div>
                        </div>
                    ))
                }
            </div>

            <nav className="navbar navbar-dark fixed-bottom d-flex  justify-content-center">
                <div className='d-flex flex-row'>
                    <input type="text"
                           onChange={(e) => setTitle(e.target.value)}
                           className="form-control col-sm-8"
                           placeholder="Start new article"/>
                    <button
                        type="submit"
                        onClick={postArticle}
                        className="btn btn-dark col-sm-4">Submit
                    </button>
                </div>
            </nav>

        </>


    )
}

export default ArticleList;