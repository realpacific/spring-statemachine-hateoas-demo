import {useContext, useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {useParams} from "react-router-dom";
import {buildRequestFromLink, parseState} from "../utils";
import {LoaderContext} from "../AppContext";

const BASE_URL = "http://localhost:8080/articles"

const ArticleDetail = (props) => {
    const {id} = useParams();
    const [article, setArticle] = useState(null);
    const [tasks, setTasks] = useState(null);
    const [_, setLoading] = useContext(LoaderContext);

    const fetchTasks = async (link) => {
        const response = await buildRequestFromLink(link)
        const data = response.data.sort((a, b) => a.rel.localeCompare(b.rel))
        setTasks(data)
    }
    const fetchArticleDetail = async () => {
        try {

            setLoading(true);
            const response = await axios.get(BASE_URL + '/' + id)
            const data = response.data
            setArticle(data)
            await fetchTasks(data._links.tasks)
        } finally {
            setLoading(false);
        }
    }
    const handleTaskClick = async (task) => {
        setLoading(true)
        const response = await buildRequestFromLink(task)
        if (response.status === 200) await fetchArticleDetail()
        setLoading(false)
    }

    useEffect(() => {
        (async () => await fetchArticleDetail())()
    }, [])

    return (
        <div className={'mx-5 mt-3'}>
            {article != null ?
                <div className="d-flex flex-column">
                    <h1 className='fw-bolder'>{article.title}</h1>
                    <span className="fw-bold text-black-50 font-monospace">{parseState(article.state)}</span>
                    <span className="text-black-50">{article.updatedDate}</span>
                    <div className="bg-primary dropdown-divider"/>
                    <p className="mt-3">{article.body}</p>
                </div> : <></>
            }
            <div className="d-flex justify-content-end">
                {tasks ? tasks.map(it =>
                    <button
                        className={'btn btn-outline-primary m-1 fw-bolder'}
                        onClick={() => handleTaskClick(it)}
                    >
                        {parseState(it.rel)}
                    </button>
                ) : <></>}

            </div>
        </div>
    )
}

export default ArticleDetail;