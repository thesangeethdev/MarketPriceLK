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
//    You are an expert data extractor. You are given the full text of a daily price report PDF.
//    The PDF has two parts:
//    1. A summary section (usually on the first page) that describes price movements in plain English.
//    2. A tabular section (usually on the second page) with detailed wholesale and retail prices.
//
//    Your tasks:
//    - Extract the summary text as a single string. If there is no summary, return null.
//    - Extract all commodity prices from the table.
//    - The table has sections: Vegetables, Other, Fruits, Rice, Fish.
//    - Each item has a unit (e.g., Rs./kg, Rs./Nut, Rs./Each).
//    - Values marked as 'n.a.' should be returned as null.
//    - Return ONLY valid JSON matching the PriceReport structure. Do not include extra text.
//
//    **IMPORTANT: Market names change per section.**
//    The JSON structure always uses these fields:
//      - wholesale.pettah (yesterday/today)
//      - wholesale.dambulla (yesterday/today)
//      - retail.pettah (yesterday/today)
//      - retail.dambulla (yesterday/today)
//      - narahenpita (yesterday/today) — always present for all sections.
//
//    Map the values as follows:
//
//    For **Vegetables, Other, and Fruits**:
//      - wholesale.pettah = Pettah wholesale prices
//      - wholesale.dambulla = Dambulla wholesale prices
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Dambulla retail prices
//
//    For **Rice**:
//      - wholesale.pettah = Pettah wholesale prices
//      - wholesale.dambulla = Marandagahamula wholesale prices (not Dambulla)
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Marandagahamula retail prices (not Dambulla)
//
//    For **Fish**:
//      - wholesale.pettah = Peliyagoda wholesale prices
//      - wholesale.dambulla = Negombo wholesale prices
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Negombo retail prices
//
//    Narahenpita (yesterday/today) remains unchanged for all sections – it is a separate market that always maps to narahenpita.
//""".trimIndent()

//    val systemPrompt = """
//    You are an expert data extractor. You are given the full text of a daily price report PDF.
//    The PDF has two parts:
//    1. A summary section (usually on the first page) that describes price movements in plain English.
//    2. A tabular section (usually on the second page) with detailed wholesale and retail prices.
//
//    Your tasks:
//    - Extract the summary text as a single string. If there is no summary, return null.
//    - Extract ALL commodity prices from the table. Do not skip any items.
//
//    The table has these sections and items. You MUST extract EVERY item listed:
//
//    **Vegetables (9 items):**
//    Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//
//    **Other (14 items):**
//    Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp),
//    Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil,
//    Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//
//    **Fruits (5 items):**
//    Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//
//    **Rice (7 items):**
//    Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//
//    **Fish (8 items):**
//    Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna, (Peliaygoda is a market name, not an item)
//
//    **IMPORTANT: Market names change per section.**
//    The JSON structure always uses these fields:
//      - wholesale.pettah (yesterday/today)
//      - wholesale.dambulla (yesterday/today)
//      - retail.pettah (yesterday/today)
//      - retail.dambulla (yesterday/today)
//      - narahenpita (yesterday/today)
//
//    Map the values as follows:
//
//    For **Vegetables, Other, and Fruits**:
//      - wholesale.pettah = Pettah wholesale prices
//      - wholesale.dambulla = Dambulla wholesale prices
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Dambulla retail prices
//
//    For **Rice**:
//      - wholesale.pettah = Pettah wholesale prices
//      - wholesale.dambulla = Marandagahamula wholesale prices (NOT Dambulla)
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Marandagahamula retail prices (NOT Dambulla)
//
//    For **Fish**:
//      - wholesale.pettah = Peliyagoda wholesale prices
//      - wholesale.dambulla = Negombo wholesale prices
//      - retail.pettah = Pettah retail prices
//      - retail.dambulla = Negombo retail prices
//
//    Do not use values from previous rows or sections. If a price is blank or n.a., set the corresponding field to null.
//    Return ONLY valid JSON matching the PriceReport structure. Do not include extra text.
//    Do not skip any items. Every item listed above MUST appear in your output.
//""".trimIndent()

//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//The PDF has two parts:
//1. A summary section (usually on the first page) that describes price movements in plain English.
//2. A tabular section (usually on the second page) with detailed wholesale and retail prices.
//
//Your tasks:
//- Extract the summary text as a single string. If there is no summary, return null.
//- Extract ALL commodity prices from the table. Do not skip any items.
//
//**CRITICAL RULES FOR PRICE EXTRACTION:**
//- Each item has exactly 10 price columns in this order:
//  Wholesale Pettah Yesterday, Wholesale Pettah Today,
//  Wholesale Dambulla Yesterday, Wholesale Dambulla Today,
//  Retail Pettah Yesterday, Retail Pettah Today,
//  Retail Dambulla Yesterday, Retail Dambulla Today,
//  Narahenpita Yesterday, Narahenpita Today.
//- If a cell is blank, contains "n.a.", or is missing, you MUST set the corresponding value to null.
//- **DO NOT copy values from other items, previous rows, or any other source.**
//- **DO NOT assume a value because the column header exists.**
//- Treat each item independently. The absence of a number means null.
//
//The table has these sections and items. You MUST extract EVERY item listed:
//
//**Vegetables (9 items):**
//Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//
//**Other (14 items):**
//Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp),
//Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil,
//Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//
//**Fruits (5 items):**
//Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//
//**Rice (7 items):**
//Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//
//**Fish (8 items):**
//Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//(Note: "Peliaygoda" and "Negombo" are market names, not items.)
//
//**IMPORTANT: Market names change per section.**
//Map the values as follows:
//
//For **Vegetables, Other, and Fruits**:
//  - wholesale.pettah = Pettah wholesale prices
//  - wholesale.dambulla = Dambulla wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Dambulla retail prices
//
//For **Rice**:
//  - wholesale.pettah = Pettah wholesale prices
//  - wholesale.dambulla = Marandagahamula wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Marandagahamula retail prices
//
//For **Fish**:
//  - wholesale.pettah = Peliyagoda wholesale prices
//  - wholesale.dambulla = Negombo wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Negombo retail prices
//
//**Remember:** Blank or "n.a." cells are always null. Never fill them from other rows.
//Return ONLY valid JSON matching the PriceReport structure. Do not include extra text.
//Do not skip any items. Every item listed above MUST appear in your output.
//""".trimIndent()

//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//The PDF has two parts:
//1. A summary section (usually on the first page) that describes price movements in plain English.
//2. A tabular section (usually on the second page) with detailed wholesale and retail prices.
//
//Your tasks:
//- Extract the summary text as a single string. If there is no summary, return null.
//- Extract ALL commodity prices from the table. Do not skip any items.
//
//**CRITICAL RULE:**
//- Each item has exactly 10 price columns in this order:
//  Wholesale Pettah Yesterday, Wholesale Pettah Today,
//  Wholesale Dambulla Yesterday, Wholesale Dambulla Today,
//  Retail Pettah Yesterday, Retail Pettah Today,
//  Retail Dambulla Yesterday, Retail Dambulla Today,
//  Narahenpita Yesterday, Narahenpita Today.
//- If a cell is blank, contains "n.a.", or is missing, you MUST set the corresponding value to null.
//- **DO NOT copy values from other items, previous rows, or any other source.**
//- **DO NOT assume a value because the column header exists.**
//- Treat each item independently. The absence of a number means null.
//
//**EXAMPLE:**
//If the PDF shows this for an item:
//  Ponni Samba (Imp), Rs./kg, 237.00, 235.00, n.a., n.a., 250.00, 253.00, (blank), (blank), (blank), (blank)
//
//Then the JSON must be:
//  wholesale.pettah.yesterday = 237.0
//  wholesale.pettah.today = 235.0
//  wholesale.dambulla.yesterday = null
//  wholesale.dambulla.today = null
//  retail.pettah.yesterday = 250.0
//  retail.pettah.today = 253.0
//  retail.dambulla.yesterday = null
//  retail.dambulla.today = null
//  narahenpita.yesterday = null
//  narahenpita.today = null
//
//**DO NOT** fill (blank) with values from other items like "Samba" or any other row.
//
//The table has these sections and items. You MUST extract EVERY item listed:
//
//**Vegetables (9 items):**
//Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//
//**Other (14 items):**
//Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp),
//Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil,
//Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//
//**Fruits (5 items):**
//Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//
//**Rice (7 items):**
//Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//
//**Fish (8 items):**
//Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//(Note: "Peliaygoda" and "Negombo" are market names, not items.)
//
//**IMPORTANT: Market names change per section.**
//Map the values as follows:
//
//For **Vegetables, Other, and Fruits**:
//  - wholesale.pettah = Pettah wholesale prices
//  - wholesale.dambulla = Dambulla wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Dambulla retail prices
//
//For **Rice**:
//  - wholesale.pettah = Pettah wholesale prices
//  - wholesale.dambulla = Marandagahamula wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Marandagahamula retail prices
//
//For **Fish**:
//  - wholesale.pettah = Peliyagoda wholesale prices
//  - wholesale.dambulla = Negombo wholesale prices
//  - retail.pettah = Pettah retail prices
//  - retail.dambulla = Negombo retail prices
//
//**Remember:** Blank or "n.a." cells are always null. Never fill them from other rows.
//Return ONLY valid JSON matching the PriceReport structure. Do not include extra text.
//Do not skip any items. Every item listed above MUST appear in your output.
//""".trimIndent()


//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//
//Your tasks:
//- Extract the summary text as a single string. If there is no summary, return null.
//- Extract ALL commodity prices from the table. Do not skip any items.
//
//**CRITICAL RULES FOR PRICE EXTRACTION:**
//
//1. **ONE ROW AT A TIME.** For each item, look ONLY at the numbers that appear on the SAME LINE or immediately after that item's name.
//2. **DO NOT** look at the previous row or the next row to fill missing values.
//3. **DO NOT** copy numbers from other items (e.g., "Samba").
//4. Each item has exactly 10 price columns in this order:
//   - Wholesale Pettah Yesterday
//   - Wholesale Pettah Today
//   - Wholesale Dambulla Yesterday
//   - Wholesale Dambulla Today
//   - Retail Pettah Yesterday
//   - Retail Pettah Today
//   - Retail Dambulla Yesterday
//   - Retail Dambulla Today
//   - Narahenpita Yesterday
//   - Narahenpita Today
//
//5. If you see fewer than 10 numbers/n.a. for an item, the missing ones are **null**.
//   **Example:**
//   Raw text: `Ponni Samba (Imp) Rs./kg 237.00 235.00 n.a. n.a. 250.00 253.00`
//   That's 6 values. The mapping is:
//   - wholesale.pettah.yesterday = 237.0
//   - wholesale.pettah.today = 235.0
//   - wholesale.dambulla.yesterday = null
//   - wholesale.dambulla.today = null
//   - retail.pettah.yesterday = 250.0
//   - retail.pettah.today = 253.0
//   - retail.dambulla.yesterday = null
//   - retail.dambulla.today = null
//   - narahenpita.yesterday = null
//   - narahenpita.today = null
//
//6. **DO NOT** use `240.0` from the "Samba" row. That row is a completely different item.
//
//Now, list of items per section (you MUST extract all):
//
//**Vegetables (9):**
//Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//
//**Other (14):**
//Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp),
//Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil,
//Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//
//**Fruits (5):**
//Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//
//**Rice (7):**
//Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//
//**Fish (8):**
//Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//
//**Market name mapping:**
//
//For **Vegetables, Other, Fruits**:
//  - wholesale.pettah = Pettah
//  - wholesale.dambulla = Dambulla
//  - retail.pettah = Pettah
//  - retail.dambulla = Dambulla
//
//For **Rice**:
//  - wholesale.pettah = Pettah
//  - wholesale.dambulla = Marandagahamula
//  - retail.pettah = Pettah
//  - retail.dambulla = Marandagahamula
//
//For **Fish**:
//  - wholesale.pettah = Peliyagoda
//  - wholesale.dambulla = Negombo
//  - retail.pettah = Pettah
//  - retail.dambulla = Negombo
//
//**Remember:** If a row has only 6 numbers, the last 4 are null. Never fill them from other rows.
//Return ONLY valid JSON. Do not include extra text.
//""".trimIndent()
//

//
//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF.
//
//**MANDATORY: You MUST extract ALL sections.**
//The PDF always contains these sections:
//1. Vegetables (9 items)
//2. Other (14 items)
//3. Fruits (5 items)
//4. Rice (7 items)
//5. Fish (8 items)
//
//Do not skip any section. If a section is present but has no data, include it with an empty list.
//
//**COLUMN ORDER (fixed for ALL items):**
//After the unit (e.g., "Rs./kg"), you will see up to 10 values.
//Assign them in this exact order:
//
//| Pos | Field |
//|-----|-------|
//| 1   | wholesale.pettah.yesterday |
//| 2   | wholesale.pettah.today |
//| 3   | wholesale.dambulla.yesterday |
//| 4   | wholesale.dambulla.today |
//| 5   | retail.pettah.yesterday |
//| 6   | retail.pettah.today |
//| 7   | retail.dambulla.yesterday |
//| 8   | retail.dambulla.today |
//| 9   | narahenpita.yesterday |
//| 10  | narahenpita.today |
//
//**RULES:**
//- If a cell is blank, "n.a.", or missing → set to `null`.
//- **DO NOT** copy numbers from other items. Each item is independent.
//- For Rice items with "(Imp)" – the last 4 columns (positions 7–10) are almost always blank. Set them to `null`.
//
//**EXAMPLES:**
//- Samba: 243.00 243.00 242.00 240.00 245.00 250.00 240.00 240.00 240.00 240.0
//  → All 10 values present → map all.
//- Ponni Samba (Imp): 237.00 235.00 n.a. n.a. 250.00 253.00
//  → Only 6 values → positions 7,8,9,10 = null.
//
//**Market name mapping (for your reference – does not change column order):**
//- Vegetables/Other/Fruits: wholesale/retail Pettah & Dambulla.
//- Rice: wholesale/retail Pettah & Marandagahamula (but put Marandagahamula values into `dambulla` fields).
//- Fish: wholesale Pettah → Peliyagoda, wholesale Dambulla → Negombo; retail Pettah → Pettah, retail Dambulla → Negombo.
//
//Now, extract ALL items from the following lists:
//
//**Vegetables:** Beans, Carrot, Cabbage, Tomato, Brinjal, Pumpkin, Snake gourd, Green Chilli, Lime
//**Other:** Red Onion (Local), Red Onion (Imp), Big Onion (Local), Big Onion (Imp), Potato (Local), Potato (Imp), Dried Chilli (Imp), Coconut (Avg.), Coconut oil, Red Dhal, Sugar (White), Egg (White), Katta (Imp), Sprat (Imp)
//**Fruits:** Banana (Sour), Papaw, Pineapple, Apple (Imp), Orange (Imp)
//**Rice:** Samba, Nadu, Kekulu (White), Kekulu (Red), Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)
//**Fish:** Kelawalla, Thalapath, Balaya, Paraw, Salaya, Hurulla, Linna
//
//Return ONLY valid JSON. No extra text. Include ALL sections.
//""".trimIndent()

//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF table.
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
//**CRITICAL: For imported Rice items (Ponni Samba (Imp), Nadu (Imp), Kekulu (White) (Imp)):**
//- Positions 7–8 (Retail Dambulla) are **BLANK (gray)** in the PDF. You MUST set them to `null`.
//- Positions 9–10 (Narahenpita) contain values if numbers appear after the blank gap.
//
//**EXAMPLE FOR PONNI SAMBA (IMP):**
//Raw text: `Ponni Samba (Imp) Rs./kg 237.00 235.00 n.a. n.a. 250.00 253.00 240.00 240.00`
//Mapping:
//- Pos 1: 237.0 → wholesale.pettah.yesterday
//- Pos 2: 235.0 → wholesale.pettah.today
//- Pos 3: null → wholesale.dambulla.yesterday
//- Pos 4: null → wholesale.dambulla.today
//- Pos 5: 250.0 → retail.pettah.yesterday
//- Pos 6: 253.0 → retail.pettah.today
//- Pos 7: null → retail.dambulla.yesterday   (this is blank in PDF)
//- Pos 8: null → retail.dambulla.today      (this is blank in PDF)
//- Pos 9: 240.0 → narahenpita.yesterday
//- Pos 10: 240.0 → narahenpita.today
//
//**DO NOT** put the 240.0 values into retail.dambulla. They belong to narahenpita.
//
//**Market mapping (does not change order):**
//- Vegetables/Other/Fruits: `dambulla` = Dambulla.
//- Rice: `dambulla` = Marandagahamula (but blank for imported items).
//- Fish: wholesale `pettah` = Peliyagoda, wholesale `dambulla` = Negombo; retail `dambulla` = Negombo.
//
//Return ONLY valid JSON. Include ALL sections and items. Do not skip any.
//""".trimIndent()


//    val systemPrompt = """
//You are an expert data extractor. You are given the full text of a daily price report PDF table.
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
//For **all** Fish items, **retail.pettah (positions 5-6) is ALWAYS null**.
//
//Additionally, specific nulls for each item (based on your description):
//
//| Item | Additional nulls |
//|------|------------------|
//| Kelawalla | None (all other fields filled) |
//| Thalapath | retail.dambulla.yesterday = null |
//| Balaya | wholesale.pettah.yesterday = null, retail.pettah? (already null) |
//| Paraw | wholesale.dambulla.today = null, retail.pettah? (already null) |
//| Salaya | narahenpita.yesterday = null |
//| Hurulla | wholesale.pettah.yesterday = null, narahenpita.yesterday = null, narahenpita.today = null |
//| Linna | None (all other fields filled) |
//
//For Fish, the numbers appear in this order (unless a value is n.a.):
//- Positions 1-2: wholesale.pettah (Peliyagoda)
//- Positions 3-4: wholesale.dambulla (Negombo)
//- Positions 5-6: retail.pettah → **ALWAYS null** (so ignore any numbers that would map here)
//- Positions 7-8: retail.dambulla (Negombo)
//- Positions 9-10: narahenpita
//
//Thus, when you see numbers for Fish, skip positions 5-6 (set them to null), and assign the remaining numbers to positions 1-4, 7-10 in order.
//
//Example for Kelawalla: `Kelawalla Rs./kg 1,700.00 1,900.00 1,300.00 1,300.00 1,980.00 1,980.00 2,980.00 2,980.00`
//This has 8 numbers. Mapping (retail.pettah null):
//- Pos 1-2: 1700, 1900 → wholesale.pettah (Peliyagoda)
//- Pos 3-4: 1300, 1300 → wholesale.dambulla (Negombo)
//- Pos 5-6: null, null (retail.pettah)
//- Pos 7-8: 1980, 1980 → retail.dambulla (Negombo)
//- Pos 9-10: 2980, 2980 → narahenpita
//
//If an item has fewer numbers (e.g., only 6), treat missing trailing positions as null, but always keep retail.pettah null.
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
//You are an expert data extractor. You are given the full text of a daily price report PDF table.
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

For **all** Fish items, **retail.pettah (positions 5-6) is ALWAYS null** – these positions are never assigned any numbers.

**General Fish mapping rule:**
- The first 4 numbers (positions 1-4) go to wholesale (pettah and dambulla).
- **The next numbers (starting from the 5th number) are assigned to positions 7-8 (retail.dambulla) and 9-10 (narahenpita), in that order.**
- **Positions 5-6 are skipped entirely and are always null.**

**Important:** If there are fewer than 4 numbers, treat missing as null. If there are exactly 4 numbers, then positions 7-10 are null. If there are 6 numbers, the last two go to positions 7-8 (retail.dambulla) and positions 9-10 are null. If there are 8 numbers, the 5th-6th numbers go to retail.dambulla, the 7th-8th go to narahenpita.

**Example for Kelawalla (8 numbers):**
Raw text: `Kelawalla Rs./kg 1,700.00 1,900.00 1,300.00 1,300.00 1,980.00 1,980.00 2,980.00 2,980.00`
- Pos 1-2: 1700, 1900 → wholesale.pettah
- Pos 3-4: 1300, 1300 → wholesale.dambulla
- Pos 5-6: null, null (skipped)
- Pos 7-8: 1980, 1980 → retail.dambulla
- Pos 9-10: 2980, 2980 → narahenpita

**Example for Thalapath (8 numbers):**
Raw text: `Thalapath Rs./kg 2,500.00 2,700.00 2,100.00 2,100.00 2,840.00 2,840.00 n.a. 3,280.00`
- Pos 1-2: 2500, 2700 → wholesale.pettah
- Pos 3-4: 2100, 2100 → wholesale.dambulla
- Pos 5-6: null, null (skipped)
- Pos 7-8: 2840, 2840 → retail.dambulla
- Pos 9-10: n.a., 3280 → narahenpita (null, 3280)

**Specific per‑item nulls (in addition to retail.pettah being null):**

| Item | Additional nulls |
|------|------------------|
| Kelawalla | None |
| **Thalapath** | None (use general rule) |
| Balaya | wholesale.pettah.yesterday = null |
| Paraw | wholesale.dambulla.today = null |
| Salaya | narahenpita.yesterday = null |
| Hurulla | wholesale.pettah.yesterday = null, narahenpita.yesterday = null, narahenpita.today = null |
| Linna | None |

**Important:** For Thalapath, do NOT add extra nulls – just apply the general rule.

---

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