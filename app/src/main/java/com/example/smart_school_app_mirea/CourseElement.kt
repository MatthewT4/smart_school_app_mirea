package com.example.smart_school_app_mirea

enum class ElementType {
    Topic,
    Test,
}


class CourseElement(var id: String, var title: String, var elementType: ElementType, topicBody: String) {
}