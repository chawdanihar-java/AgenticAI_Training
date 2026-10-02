# Shopping Assistant — Skills

## Current skills

### 1. Search Products
Finds individual products matching a free-text query using live web data.

- Trigger: any request asking to find/search for a product (e.g. "search gaming laptops under 60000").
- Data source: DuckDuckGo HTML search + fetched product pages (`searchWeb`, `fetchPage`).
- Output fields: product name/model, current price, retailer, processor, RAM, storage, display, GPU, availability, source URL.
- Rules: category/listing pages are skipped (`isLikelyCategoryPage`); no invented prices, specs, or availability — unverifiable fields are marked "Not available".

### 2. Compare Products
Compares two or more named products using the same live web data pipeline.

- Trigger: any request naming multiple products with an intent to compare (e.g. "compare X vs Y").
- Output fields: product, current price, processor, RAM, storage, display, GPU, features, reviews (if available), availability, source URL.
- Rules: only compares information actually retrieved; does not pick or recommend a winner — presents factual differences and trade-offs only.

Both skills share the same pipeline: `handleUserRequest` → `searchWeb` → `fetchPage`/`cleanHtml` → Claude analyzes the retrieved page text under `SYSTEM_PROMPT`. There is no explicit intent router — Claude infers from the user's phrasing whether the request is a search or a comparison.

## Proposed future skills

Ideas that would extend the existing search/fetch pipeline:

1. **Price tracking / price-drop alert** — re-fetch a saved product URL over time and report price changes.
2. **Review/sentiment summary** — fetch review pages for a product and summarize pros/cons.
3. **Budget-based recommendation** — filter search results by extracted price against a stated budget.
4. **Availability/stock checker** — re-check a specific product URL for in-stock status.
5. **Spec-based filtering** — filter parsed specs (RAM, GPU, etc.) before returning results.
6. **Deal/discount finder** — search specifically for coupon or sale pages for a named product.
7. **Currency/region conversion** — convert non-INR prices to INR for consistency.
