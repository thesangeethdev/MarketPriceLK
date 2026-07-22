package com.sangeeth

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor

suspend fun extractPriceData(pdfText: String): PriceReport{
    val systemPrompt = """
        You are an expert data extractor. You are given the full text of a daily price report PDF.
        The PDF has two parts:
        1. A summary section (usually on the first page) that describes price movements in plain English.
        2. A tabular section (usually on the second page) with detailed wholesale and retail prices.

        Your tasks:
        - Extract the summary text as a single string. If there is no summary, return null.
        - Extract all commodity prices from the table.
        - The table has sections: Vegetables, Other, Fruits, Rice, Fish.
        - Each item has a unit (e.g., Rs./kg, Rs./Nut, Rs./Each).
        - There are 10 price columns in this exact order:
          Wholesale Pettah Yesterday, Wholesale Pettah Today,
          Wholesale Dambulla Yesterday, Wholesale Dambulla Today,
          Retail Pettah Yesterday, Retail Pettah Today,
          Retail Dambulla Yesterday, Retail Dambulla Today,
          Narahenpita Yesterday, Narahenpita Today.
        - Values marked as 'n.a.' should be returned as null.
        - Return ONLY valid JSON matching the PriceReport structure. Do not include extra text.
    """.trimIndent()

    val strategy = functionalStrategy<String, PriceReport> { input ->
        val fullPrompt = """
            $systemPrompt
            PDF Text:
            $input
        """.trimIndent()

        val response = requestLLMStructured<PriceReport>(
            message = fullPrompt,
            examples = emptyList(),
            fixingParser = null
        )
        response.getOrThrow().data
    }

    val client = OpenAILLMClient(apiKey = System.getenv("OPENAI_API_KEY"))
    val promptExecutor = MultiLLMPromptExecutor(client)

    val agentConfig = AIAgentConfig(
        prompt = prompt("extract_prices"){
            system(systemPrompt)
        },
        model = OpenAIModels.Chat.GPT4o,
        maxAgentIterations = 1
    )

    val emptyRegistry = ToolRegistry.builder().build()

    val agent = AIAgent(
        promptExecutor = promptExecutor,
        strategy = strategy,
        agentConfig = agentConfig,
        toolRegistry = emptyRegistry
    )

    return agent.run(pdfText)
}