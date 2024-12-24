package com.example.smart_school_app_mirea

enum class ElementType {
    Topic,
    Test,
}


data class CourseElement(
    val id: String,
    val title: String,
    val elementType: ElementType,
    val topicBody: String? = null, // Только для топиков
    val maxScore: Int? = null,     // Только для тестов
    val resultScore: Int? = null,  // Только для тестов
    val testElements: List<TestElement>? = null // Только для тестов
)