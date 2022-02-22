import {useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {Link} from "react-router-dom";
import {parseState} from "../utils";

const BASE_URL = "http://localhost:8080/articles"

const ArticleList = (props) => {
    const [articles, setArticles] = useState([])

    useEffect(() => {
        (async function fetchArticleData() {
            const response = await axios.get(BASE_URL)
            setArticles(response.data)
        })()
    }, [])

    return (
        <div className='row'>
            {
                articles.map(it => (
                    <div className='col-lg-3'>
                        <div className='card'>
                            <div className='card-body'>
                                <h5 className='card-title'>
                                    <strong>{it.title}</strong>
                                </h5>
                                <h6 className='card-subtitle'>{parseState(it.state)}</h6>
                                <div className='card-text'>
                                        <span className={'list-element__single-line'}>
                                        <small>{it.body}</small>
                                    </span>
                                </div>
                                <Link to={`${it.id}`} style={{textDecoration: 'none'}}>
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