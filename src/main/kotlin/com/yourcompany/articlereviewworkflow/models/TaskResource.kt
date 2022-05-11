package com.yourcompany.articlereviewworkflow.models

import org.springframework.hateoas.RepresentationModel

open class TaskResource(val name: String) : RepresentationModel<TaskResource>()