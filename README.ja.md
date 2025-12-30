# 日本語翻訳練習・評価システム

[English Version → README.md](./README.md)

---

## 概要

本プロジェクトは、日本語学習者向けの  
**翻訳練習および自動評価システム**です。

学習者が日本語で翻訳した文を入力すると、  
LLM（Ollama）を用いて以下を自動で評価します。

- 文法の正確さ
- 助詞の使い方
- 表現の自然さ
- 総合スコア

評価結果を保存し、次の練習用文を提示することで、  
**継続的な日本語学習を支援**します。

---

## 主な機能

- 日本語翻訳文の自動評価（LLM 利用）
- 正誤判定（Correct / Incorrect）
- 評価結果の履歴保存
- 学習進捗（現在の文）管理
- 次の練習文の自動提示
- Ollama API 互換レスポンス（ollama-webui-lite 対応）

---

## システム構成

Client [(ollama-webui-lite)](https://github.com/joshaken/ollama-webui-lite)
⬇️
Spring Gateway
⬇️
Spring WebFlux API
⬇️
[Ollama](https://github.com/ollama/ollama) (LLM)
⬇️
H2 Database

---

## 使用技術

### バックエンド
- Java 17
- Spring Boot
- Spring WebFlux
- Spring Gateway
- R2DBC
- Project Reactor

### LLM
- Ollama
- Streaming / Non-Streaming 両対応

### データベース
- H2 Database / postgres

---

## 処理フロー

1. ユーザーが翻訳文を入力
2. 現在の練習文を取得
3. 翻訳結果を LLM に送信
4. 評価結果を解析
5. 学習履歴をデータベースに保存
6. 次の練習文を返却

![UML.png](UML.png)
---

## データベース設計（概要）

- users
- sentences
- user_answer_record

ユーザーごとの学習進捗と回答履歴を管理します。

---

## 特徴・工夫点

- **Reactive Programming（非同期処理）**
    - WebFlux + Reactor を使用
    - LLM のストリーミング応答を安全に制御

- **LLM レスポンスの制御**
    - 評価用レスポンスは内部処理
    - フロントエンドには Ollama 互換形式で返却

- **学習向け設計**
    - 正誤判定のみを保存し、評価ロジックを柔軟に拡張可能

---

## 開発目的

- Reactive Stack の実践的な理解
- LLM を利用したバックエンド設計の検証
- 日本語学習支援システムの構築

---

## 今後の改善予定

- ユーザー管理機能の拡張
- 難易度別の文管理
- 評価基準のカスタマイズ
- 

---

## ライセンス

This project is for learning and demonstration purposes.
