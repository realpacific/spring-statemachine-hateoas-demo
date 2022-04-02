import {useContext, useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {useParams} from "react-router-dom";
import {buildRequestFromLink, format} from "../utils";
import {LoaderContext} from "../AppContext";
import {ARTICLES_BASE_URL} from "../constants";

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
            const response = await axios.get(ARTICLES_BASE_URL + "/" + id)
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

    const handleSaveClick = async () => {
        setLoading(true);
        await buildRequestFromLink({
                ...article._links.update,
                data: article
            }
        )
        await fetchArticleDetail()
    }

    return (
        <div className={"mx-5 mt-3"}>
            <div className="d-flex flex-column" style={{height: "80vh"}}>
                {article && (<>
                    <h1 className="fw-bolder">{article.title}</h1>

                    <div>
                        <span className="fw-bold text-black-50 font-monospace"><strong>{format(article.state)}</strong></span>
                        <span className="fw-bold text-black-50 font-monospace mx-2">|</span>
                        <span
                            className="fw-bold text-black-50 font-monospace"><strong>{format(article.reviewType)}</strong></span>
                    </div>
                    <span className="text-black-50">{article.updatedDate}</span>
                    <div className="bg-primary dropdown-divider"/>
                    <textarea className="form-control mt-3 flex-fill flex-grow-1"
                              disabled={article?._links?.update == null}
                              onChange={e => setArticle({...article, body: e.target.value})}
                              value={article.body}/>
                </>)
                }
                <div className="d-flex align-self-end justify-content-end">
                    {
                        article?._links?.update && <button
                            className={"btn btn-outline-primary m-1 fw-bolder"}
                            onClick={() => handleSaveClick()}
                        >Save</button>
                    }
                    {tasks && tasks.map(it =>
                        <button
                            className={"btn btn-outline-primary m-1 fw-bolder"}
                            onClick={() => handleTaskClick(it)}
                        >
                            {format(it.rel)}
                        </button>
                    )}

                </div>
            </div>
        </div>
    )
}

export default ArticleDetail;