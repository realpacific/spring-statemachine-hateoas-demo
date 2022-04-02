import {useContext, useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {Link} from "react-router-dom";
import {format} from "../utils";
import {LoaderContext} from "../AppContext";
import {ARTICLES_BASE_URL} from "../constants";

const ArticleList = (props) => {
    const [articles, setArticles] = useState([])
    const [_, setIsLoading] = useContext(LoaderContext)
    const [title, setTitle] = useState(undefined)

    const fetchArticles = async () => {
        setIsLoading(true)
        const response = await axios.get(ARTICLES_BASE_URL)
        setArticles(response.data)
        setIsLoading(false)
    };

    useEffect(async () => {
        await fetchArticles()
    }, [])


    const postArticle = async () => {
        setIsLoading(true)
        const response = await axios.post(ARTICLES_BASE_URL, {title, body: ""})
        if (response.status === 200) {
            setTitle(undefined)
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
                                    <span className="card-subtitle opacity-75 fw-bold">{format(it.state)}</span>
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