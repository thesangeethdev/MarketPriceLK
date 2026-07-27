package com.sangeeth

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor

suspend fun extractPriceData(pdfText: String): PriceReport {

//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//
//**SUMMARY EXTRACTION INSTRUCTION:**
//- The PDF has a **summary section** on the first page – a narrative text describing price movements for various commodities in plain English. It typically starts with a title like "A Summary of Price Developments" or "Price Summary".
//- **Your task:** Extract the **entire narrative summary** as a single string, excluding the title itself. Include all descriptive sentences that explain price changes (e.g., "Price of Beans declined ... due to ...").
//- If there is no summary, return `null`.
//- **Ignore** any numerical data or table‑like content that may appear on the first page – only capture the natural‑language sentences.
//
//**Example:**
//Given the text: "A Summary of Price Developments - 22 July 2026\nPrice of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."
//The summary should be: "Price of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."
//
//Now, for the rest of the extraction, follow the rules below exactly as given.
//
//---
//
//**MANDATORY: Extract ALL items from ALL sections.**
//
//Sections and items (you MUST include every single one):
//
//Vegetables (9): Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//Other (14): Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp), Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil, Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//Fruits (5): Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//Rice (7): Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//Fish (8): Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//
//**FIXED COLUMN ORDER (10 positions, in this exact order):**
//1. wholesale.pettah.yesterday
//2. wholesale.pettah.today
//3. wholesale.dambulla.yesterday   (for Rice: Marandagahamula)
//4. wholesale.dambulla.today      (for Rice: Marandagahamula)
//5. retail.pettah.yesterday
//6. retail.pettah.today
//7. retail.dambulla.yesterday     (for Rice: Marandagahamula)
//8. retail.dambulla.today        (for Rice: Marandagahamula)
//9. narahenpita.yesterday
//10. narahenpita.today
//
//---
//
//**GENERAL RULE:** For items not explicitly listed below, use the standard mapping: if the item has fewer than 10 numbers, the missing trailing positions become null. But for the items listed below, follow the **exact mapping** provided.
//
//---
//
//**EXPLICIT MAPPING FOR SPECIFIC ITEMS:**
//
//### 1. OTHER SECTION
//
//For the following items, the numbers appear in this order:
//**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**
//All Dambulla columns (positions 3,4,7,8) are **null**.
//
//Items:
//- Coconut oil
//- Red Dhal
//- Sugar (White)
//- Egg (White)
//- Katta (Imp)
//- Sprat (Imp)
//
//Example: `Coconut oil Rs./Ltr 802.00 802.00 884.00 884.00 852.00 852.00`
//Mapping:
//- Pos 1-2: 802.0, 802.0 → wholesale.pettah
//- Pos 3-4: null, null → wholesale.dambulla
//- Pos 5-6: 884.0, 884.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla
//- Pos 9-10: 852.0, 852.0 → narahenpita
//
//---
//
//### 2. FRUITS SECTION
//
//For the following items, the numbers appear in this order:
//**Retail Pettah (2) → Narahenpita (2)**
//All wholesale columns (positions 1-4) and retail.dambulla (positions 7-8) are **null**.
//
//Items:
//- Apple (Imp)
//- Orange (Imp)
//
//Example: `Apple (Imp) Rs./Each 195.00 195.00 250.00 250.00`
//Mapping:
//- Pos 1-4: null, null, null, null → wholesale (pettah & dambulla)
//- Pos 5-6: 195.0, 195.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla
//- Pos 9-10: 250.0, 250.0 → narahenpita
//
//---
//
//### 3. RICE SECTION
//
//For imported Rice items (Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)), the numbers appear in this order:
//**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**
//All Dambulla columns (positions 3,4,7,8) are **null**.
//
//Example: `Ponni Samba (Imp) Rs./kg 237.00 235.00 n.a. n.a. 250.00 253.00 240.00 240.00`
//Mapping:
//- Pos 1-2: 237.0, 235.0 → wholesale.pettah
//- Pos 3-4: null, null → wholesale.dambulla (Marandagahamula)
//- Pos 5-6: 250.0, 253.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla (Marandagahamula)
//- Pos 9-10: 240.0, 240.0 → narahenpita
//
//For the other Rice items (Samba, Nadu, Kekulu (White), Kekulu (Red)) that have 10 numbers, use the standard mapping (no special rule).
//
//---
//
//### 4. FISH SECTION
//
//For **all** Fish items, **retail.pettah (positions 5-6) is ALWAYS null** – these positions are never assigned any numbers.
//
//**General Fish mapping rule:**
//- The first 4 numbers (positions 1-4) go to wholesale (pettah and dambulla).
//- **The next numbers (starting from the 5th number) are assigned to positions 7-8 (retail.dambulla) and 9-10 (narahenpita), in that order.**
//- **Positions 5-6 are skipped entirely and are always null.**
//
//**Important:** If there are fewer than 4 numbers, treat missing as null. If there are exactly 4 numbers, then positions 7-10 are null. If there are 6 numbers, the last two go to positions 7-8 (retail.dambulla) and positions 9-10 are null. If there are 8 numbers, the 5th-6th numbers go to retail.dambulla, the 7th-8th go to narahenpita.
//
//**Example for Kelawalla (8 numbers):**
//Raw text: `Kelawalla Rs./kg 1,700.00 1,900.00 1,300.00 1,300.00 1,980.00 1,980.00 2,980.00 2,980.00`
//- Pos 1-2: 1700, 1900 → wholesale.pettah
//- Pos 3-4: 1300, 1300 → wholesale.dambulla
//- Pos 5-6: null, null (skipped)
//- Pos 7-8: 1980, 1980 → retail.dambulla
//- Pos 9-10: 2980, 2980 → narahenpita
//
//**Example for Thalapath (8 numbers):**
//Raw text: `Thalapath Rs./kg 2,500.00 2,700.00 2,100.00 2,100.00 2,840.00 2,840.00 n.a. 3,280.00`
//- Pos 1-2: 2500, 2700 → wholesale.pettah
//- Pos 3-4: 2100, 2100 → wholesale.dambulla
//- Pos 5-6: null, null (skipped)
//- Pos 7-8: 2840, 2840 → retail.dambulla
//- Pos 9-10: n.a., 3280 → narahenpita (null, 3280)
//
//**Specific per‑item nulls (in addition to retail.pettah being null):**
//
//| Item | Additional nulls |
//|------|------------------|
//| Kelawalla | None |
//| **Thalapath** | None (use general rule) |
//| Balaya | wholesale.pettah.yesterday = null |
//| Paraw | wholesale.dambulla.today = null |
//| Salaya | narahenpita.yesterday = null |
//| Hurulla | wholesale.pettah.yesterday = null, narahenpita.yesterday = null, narahenpita.today = null |
//| Linna | None |
//
//**Important:** For Thalapath, do NOT add extra nulls – just apply the general rule.
//
//---
//
//**FOR ALL OTHER ITEMS** (not listed above), use the standard mapping: numbers appear in order positions 1-10. If fewer than 10 numbers, the missing ones are null.
//
//**ADDITIONAL RULES:**
//- "n.a." or blank → always null.
//- Do not copy values from other items.
//- Return ONLY valid JSON. Include ALL sections and items.
//""".trimIndent()

//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//
//**SUMMARY EXTRACTION INSTRUCTION:**
//- The PDF has a **summary section** on the first page – a narrative text describing price movements for various commodities in plain English. It typically starts with a title like "A Summary of Price Developments" or "Price Summary".
//- **Your task:** Extract the **entire narrative summary** as a single string, excluding the title itself. Include all descriptive sentences that explain price changes (e.g., "Price of Beans declined ... due to ...").
//- If there is no summary, return `null`.
//- **Ignore** any numerical data or table‑like content that may appear on the first page – only capture the natural‑language sentences.
//
//**Example:**
//Given the text: "A Summary of Price Developments - 22 July 2026\nPrice of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."
//The summary should be: "Price of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."
//
//Now, for the rest of the extraction, follow the rules below exactly as given.
//
//---
//
//**MANDATORY: Extract ALL items from ALL sections.**
//
//Sections and items (you MUST include every single one):
//
//Vegetables (9): Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//Other (14): Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp), Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil, Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//Fruits (5): Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//Rice (7): Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//Fish (8): Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//
//**FIXED COLUMN ORDER (10 positions, in this exact order):**
//1. wholesale.pettah.yesterday
//2. wholesale.pettah.today
//3. wholesale.dambulla.yesterday   (for Rice: Marandagahamula)
//4. wholesale.dambulla.today      (for Rice: Marandagahamula)
//5. retail.pettah.yesterday
//6. retail.pettah.today
//7. retail.dambulla.yesterday     (for Rice: Marandagahamula)
//8. retail.dambulla.today        (for Rice: Marandagahamula)
//9. narahenpita.yesterday
//10. narahenpita.today
//
//---
//
//**GENERAL RULE:** For items not explicitly listed below, use the standard mapping: if the item has fewer than 10 numbers, the missing trailing positions become null. But for the items listed below, follow the **exact mapping** provided.
//
//---
//
//**EXPLICIT MAPPING FOR SPECIFIC ITEMS:**
//
//### 1. OTHER SECTION
//
//For the following items, the numbers appear in this order:
//**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**
//All Dambulla columns (positions 3,4,7,8) are **null**.
//
//Items:
//- Coconut oil
//- Red Dhal
//- Sugar (White)
//- Egg (White)
//- Katta (Imp)
//- Sprat (Imp)
//
//Example: `Coconut oil Rs./Ltr 802.00 802.00 884.00 884.00 852.00 852.00`
//Mapping:
//- Pos 1-2: 802.0, 802.0 → wholesale.pettah
//- Pos 3-4: null, null → wholesale.dambulla
//- Pos 5-6: 884.0, 884.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla
//- Pos 9-10: 852.0, 852.0 → narahenpita
//
//---
//
//### 2. FRUITS SECTION
//
//For the following items, the numbers appear in this order:
//**Retail Pettah (2) → Narahenpita (2)**
//All wholesale columns (positions 1-4) and retail.dambulla (positions 7-8) are **null**.
//
//Items:
//- Apple (Imp)
//- Orange (Imp)
//
//Example: `Apple (Imp) Rs./Each 195.00 195.00 250.00 250.00`
//Mapping:
//- Pos 1-4: null, null, null, null → wholesale (pettah & dambulla)
//- Pos 5-6: 195.0, 195.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla
//- Pos 9-10: 250.0, 250.0 → narahenpita
//
//---
//
//### 3. RICE SECTION
//
//For imported Rice items (Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)), the numbers appear in this order:
//**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**
//All Dambulla columns (positions 3,4,7,8) are **null**.
//
//Example: `Ponni Samba (Imp) Rs./kg 237.00 235.00 n.a. n.a. 250.00 253.00 240.00 240.00`
//Mapping:
//- Pos 1-2: 237.0, 235.0 → wholesale.pettah
//- Pos 3-4: null, null → wholesale.dambulla (Marandagahamula)
//- Pos 5-6: 250.0, 253.0 → retail.pettah
//- Pos 7-8: null, null → retail.dambulla (Marandagahamula)
//- Pos 9-10: 240.0, 240.0 → narahenpita
//
//For the other Rice items (Samba, Nadu, Kekulu (White), Kekulu (Red)) that have 10 numbers, use the standard mapping (no special rule).
//
//---
//
//### 4. FISH SECTION
//
//For **all** Fish items, **retail.pettah (positions 5-6) is ALWAYS null** – these positions are never assigned any numbers.
//
//**General Fish mapping rule:**
//- The first 4 numbers (positions 1-4) go to wholesale (pettah and dambulla).
//- **The next numbers (starting from the 5th number) are assigned to positions 7-8 (retail.dambulla) and 9-10 (narahenpita), in that order.**
//- **Positions 5-6 are skipped entirely and are always null.**
//
//**Important:** If there are fewer than 4 numbers, treat missing as null. If there are exactly 4 numbers, then positions 7-10 are null. If there are 6 numbers, the last two go to positions 7-8 (retail.dambulla) and positions 9-10 are null. If there are 8 numbers, the 5th-6th numbers go to retail.dambulla, the 7th-8th go to narahenpita.
//
//**Example for Kelawalla (8 numbers):**
//Raw text: `Kelawalla Rs./kg 1,700.00 1,900.00 1,300.00 1,300.00 1,980.00 1,980.00 2,980.00 2,980.00`
//- Pos 1-2: 1700, 1900 → wholesale.pettah
//- Pos 3-4: 1300, 1300 → wholesale.dambulla
//- Pos 5-6: null, null (skipped)
//- Pos 7-8: 1980, 1980 → retail.dambulla
//- Pos 9-10: 2980, 2980 → narahenpita
//
//**Example for Thalapath (8 numbers):**
//Raw text: `Thalapath Rs./kg 2,500.00 2,700.00 2,100.00 2,100.00 2,840.00 2,840.00 n.a. 3,280.00`
//- Pos 1-2: 2500, 2700 → wholesale.pettah
//- Pos 3-4: 2100, 2100 → wholesale.dambulla
//- Pos 5-6: null, null (skipped)
//- Pos 7-8: 2840, 2840 → retail.dambulla
//- Pos 9-10: n.a., 3280 → narahenpita (null, 3280)
//
//**Specific per‑item nulls (in addition to retail.pettah being null):**
//
//| Item | Additional nulls |
//|------|------------------|
//| Kelawalla | None |
//| **Thalapath** | None (use general rule) |
//| Balaya | None (use general rule) |
//| Paraw | None (use general rule) |
//| Salaya | None (use general rule) |
//| Hurulla | None (use general rule) |
//| Linna | None |
//
//**Important:** For Thalapath, do NOT add extra nulls – just apply the general rule.
//
//---
//
//**FOR ALL OTHER ITEMS** (not listed above), use the standard mapping: numbers appear in order positions 1-10. If fewer than 10 numbers, the missing ones are null.
//
//**ADDITIONAL RULES:**
//- "n.a." or blank → always null.
//- Do not copy values from other items.
//- Return ONLY valid JSON. Include ALL sections and items.
//""".trimIndent()

    val systemPrompt = """
You are an expert data extractor. You are given the full text of a daily price report PDF.

**SUMMARY EXTRACTION INSTRUCTION:**
- The PDF has a **summary section** on the first page – a narrative text describing price movements for various commodities in plain English. It typically starts with a title like "A Summary of Price Developments" or "Price Summary".
- **Your task:** Extract the **entire narrative summary** as a single string, excluding the title itself. Include all descriptive sentences that explain price changes (e.g., "Price of Beans declined ... due to ..."). 
- If there is no summary, return `null`.
- **Ignore** any numerical data or table‑like content that may appear on the first page – only capture the natural‑language sentences.

**Example:**
Given the text: "A Summary of Price Developments - 22 July 2026\nPrice of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."
The summary should be: "Price of Beans declined in Dambulla market compared to yesterday due to availability of low quality varieties from Thambuttegama and Matale areas. Price of Carrot declined ..."

Now, for the rest of the extraction, follow the rules below exactly as given.

---

**MANDATORY: Extract ALL items from ALL sections.**

Sections and items (you MUST include every single one):

Vegetables (9): Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
Other (14): Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp), Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil, Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
Fruits (5): Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
Rice (7): Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
Fish (8): Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna

**FIXED COLUMN ORDER (10 positions, in this exact order):**
1. wholesale.pettah.yesterday
2. wholesale.pettah.today
3. wholesale.dambulla.yesterday   (for Rice: Marandagahamula)
4. wholesale.dambulla.today      (for Rice: Marandagahamula)
5. retail.pettah.yesterday
6. retail.pettah.today
7. retail.dambulla.yesterday     (for Rice: Marandagahamula)
8. retail.dambulla.today        (for Rice: Marandagahamula)
9. narahenpita.yesterday
10. narahenpita.today

---

**GENERAL RULE:** For items not explicitly listed below, use the standard mapping: if the item has fewer than 10 numbers, the missing trailing positions become null. But for the items listed below, follow the **exact mapping** provided.

---

**EXPLICIT MAPPING FOR SPECIFIC ITEMS:**

### 1. OTHER SECTION

For the following items, the numbers appear in this order:  
**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**  
All Dambulla columns (positions 3,4,7,8) are **null**.

Items:
- Coconut oil
- Red Dhal
- Sugar (White)
- Egg (White)
- Katta (Imp)
- Sprat (Imp)

Example: `Coconut oil Rs./Ltr 802.00 802.00 884.00 884.00 852.00 852.00`  
Mapping:
- Pos 1-2: 802.0, 802.0 → wholesale.pettah
- Pos 3-4: null, null → wholesale.dambulla
- Pos 5-6: 884.0, 884.0 → retail.pettah
- Pos 7-8: null, null → retail.dambulla
- Pos 9-10: 852.0, 852.0 → narahenpita

---

### 2. FRUITS SECTION

For the following items, the numbers appear in this order:  
**Retail Pettah (2) → Narahenpita (2)**  
All wholesale columns (positions 1-4) and retail.dambulla (positions 7-8) are **null**.

Items:
- Apple (Imp)
- Orange (Imp)

Example: `Apple (Imp) Rs./Each 195.00 195.00 250.00 250.00`  
Mapping:
- Pos 1-4: null, null, null, null → wholesale (pettah & dambulla)
- Pos 5-6: 195.0, 195.0 → retail.pettah
- Pos 7-8: null, null → retail.dambulla
- Pos 9-10: 250.0, 250.0 → narahenpita

---

### 3. RICE SECTION

For imported Rice items (Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)), the numbers appear in this order:  
**Wholesale Pettah (2) → Retail Pettah (2) → Narahenpita (2)**  
All Dambulla columns (positions 3,4,7,8) are **null**.

Example: `Ponni Samba (Imp) Rs./kg 237.00 235.00 n.a. n.a. 250.00 253.00 240.00 240.00`  
Mapping:
- Pos 1-2: 237.0, 235.0 → wholesale.pettah
- Pos 3-4: null, null → wholesale.dambulla (Marandagahamula)
- Pos 5-6: 250.0, 253.0 → retail.pettah
- Pos 7-8: null, null → retail.dambulla (Marandagahamula)
- Pos 9-10: 240.0, 240.0 → narahenpita

For the other Rice items (Samba, Nadu, Kekulu (White), Kekulu (Red)) that have 10 numbers, use the standard mapping (no special rule).

---
### 4. FISH SECTION

For Fish, the numbers after the unit appear in pairs in this exact order:
1-2 → wholesale.peliyagoda (yesterday, today)
3-4 → wholesale.negombo (yesterday, today)
5-6 → retail.negombo (yesterday, today)
7-8 → narahenpita (yesterday, today)

If fewer than 8 numbers exist, the missing trailing positions become `null`.

**`retail.pettah` is ALWAYS `null` for ALL fish items – it is not part of the sequence.**

Example:
`Kelawalla Rs./kg 1,700.00 1,900.00 1,300.00 1,300.00 1,980.00 1,980.00 2,980.00 2,980.00`
→ peliyagoda = (1700, 1900), negombo = (1300, 1300), retail.negombo = (1980, 1980), narahenpita = (2980, 2980).

Apply this rule to all fish items. Do not deviate.
----

**FOR ALL OTHER ITEMS** (not listed above), use the standard mapping: numbers appear in order positions 1-10. If fewer than 10 numbers, the missing ones are null.

**ADDITIONAL RULES:**
- "n.a." or blank → always null.
- Do not copy values from other items.
- Return ONLY valid JSON. Include ALL sections and items.
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
        prompt = prompt("extract_prices") {
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