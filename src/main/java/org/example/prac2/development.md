# Shopping Assistant — Development Plan (Vertical Slices)

Each slice below is a complete, shippable end-to-end feature: user input → query/fetch logic → Claude prompt/system-prompt contract → formatted output. No slice depends on a separate "infra phase" finishing first — shared plumbing (`searchWeb`, `fetchPage`, `cleanHtml`) is extended incrementally as each slice needs it.

## Slice 1 — Search Products (done)

- Input: free-text query (e.g. "search gaming laptops under 60000").
- Pipeline: `handleUserRequest` → `searchWeb` (DuckDuckGo query + scrape) → `fetchPage`/`cleanHtml` per result → Claude call with `SYSTEM_PROMPT` search rules.
- Output: list of products with name, price, retailer, processor, RAM, storage, display, GPU, availability, source URL.
- Status: implemented in `ShoppingAgent.handleUserRequest` / `searchWeb` / `processSearchResults`.

## Slice 2 — Compare Products (done)

- Input: free-text query naming 2+ products (e.g. "compare X vs Y").
- Pipeline: same as Slice 1; Claude infers "compare" intent from phrasing and switches output shape per `SYSTEM_PROMPT` compare rules.
- Output: comparison table (product, price, specs, features, reviews, availability, source URL) with no recommended winner.
- Status: implemented, sharing all plumbing with Slice 1.

## Slice 3 — Budget-Based Recommendation

- Input: query with a budget (e.g. "laptops under 60000").
- New logic: parse budget number from user input; after Claude/extraction, filter or annotate results outside budget.
- Output: same product list as Slice 1, restricted to/flagged against the stated budget.
- Touches: `ShoppingAgent` (new budget-parsing helper), `SYSTEM_PROMPT` (add budget-filter rule).

## Slice 4 — Spec-Based Filtering

- Input: query with spec constraints (e.g. "16GB RAM, RTX GPU").
- New logic: parse requested spec constraints; filter candidate products by matching extracted specs before returning.
- Output: same product list shape as Slice 1, restricted to matching specs.
- Touches: `ShoppingAgent` (spec-matching helper), `SYSTEM_PROMPT` (spec-filter rule).

## Slice 5 — Availability / Stock Checker

- Input: a specific product name or URL, asking "is X in stock?".
- New logic: targeted single-page re-fetch (skip full search flow if a URL is given).
- Output: availability status + source URL, single-item response (not a list).
- Touches: `ShoppingAgent` (new `checkAvailability` method), `SYSTEM_PROMPT` (single-item availability rule).

## Slice 6 — Review / Sentiment Summary

- Input: a product name, asking for reviews/opinions.
- New logic: dedicated DuckDuckGo query template biased toward review sites (e.g. append "reviews").
- Output: pros/cons summary per product, sourced only from fetched review pages.
- Touches: `ShoppingAgent` (new query template + fetch path), `SYSTEM_PROMPT` (review-summary rule).

## Slice 7 — Deal / Discount Finder

- Input: a product name, asking for deals/coupons/discounts.
- New logic: query template targeting sale/coupon pages.
- Output: discount details (price, discount %, validity if shown) + source URL.
- Touches: `ShoppingAgent` (new query template), `SYSTEM_PROMPT` (deal-finder rule).

## Slice 8 — Currency / Region Conversion

- Input: any search/compare result containing a non-INR price.
- New logic: post-process extracted prices with a conversion step (static rate or lookup).
- Output: original + converted price shown together.
- Touches: `ShoppingAgent` (conversion helper), `SYSTEM_PROMPT` (currency-display rule).

## Slice 9 — Price Tracking

- Input: "track <product/url>", later "check tracked prices".
- New logic: requires persistence — store tracked URL + last known price in a local JSON file; on re-check, re-fetch and diff.
- Output: price-change report (up/down/unchanged) per tracked item.
- Touches: `ShoppingAgent` (persistence + diff logic), `ShoppingAssistantMain` (new commands: `track`, `check-tracked`).

## Cross-cutting hardening (apply alongside slices, not a blocking phase)

- Unit tests for pure-text helpers (`cleanHtml`, `cleanText`, `decodeDuckDuckGoUrl`, `isLikelyCategoryPage`, `limitText`) as each slice touches them.
- Extract HTTP/search behind a `WebSearchClient` interface once a second slice needs mockable fetch behavior (mock in tests instead of hitting live DuckDuckGo).
- Cap `history` size once multi-turn sessions using several slices in one run become common.
- Consider moving extraction to Claude tool-use (structured JSON) once 2+ slices need to filter/sort extracted data in Java rather than prose (Slices 3, 4, 9 benefit most).

## Out of scope for now

- Bypassing retailer bot protection — explicitly disallowed by current code comments and should stay that way.
- Multi-user/session support — this remains a single-session console tool.
- Explicit intent classification/router — deferred until enough slices exist that Claude inferring intent from phrasing becomes unreliable.
