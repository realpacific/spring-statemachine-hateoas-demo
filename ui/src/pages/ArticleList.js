import {useContext, useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {Link} from "react-router-dom";
import {parseState} from "../utils";
import {LoaderContext} from "../AppContext";

const BASE_URL = "http://localhost:8080/articles"

const ArticleList = (props) => {
    const [articles, setArticles] = useState([])
    const [_, setIsLoading] = useContext(LoaderContext)

    useEffect(() => {
        (async function fetchArticleData() {
            setIsLoading(true)
            const response = await axios.get(BASE_URL)
            setArticles(response.data)
            setIsLoading(false)
        })()
    }, [])

    return (
        <div className="row">
            {
                articles.map(it => (
                    <div className="col-lg-12">
                        <div className="card h-100">
                            <div className="card-body">
                                <h5 className="card-title fw-bolder">{it.title}</h5>
                                <span className="card-subtitle opacity-75 fw-bold">{parseState(it.state)}</span>
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
    )
}

export default ArticleList;