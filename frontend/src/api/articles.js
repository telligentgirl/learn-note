import { javaRequest } from './request'
// 独立 API 模块便于页面复用；详情从服务器读取，新增/更新使用 JSON。
export const listArticles = (params) => javaRequest(`/article?${new URLSearchParams(params)}`)
export const getArticle = (id) => javaRequest(`/article/detail?id=${encodeURIComponent(id)}`)
export const addArticle = (body) => javaRequest('/article', { method: 'POST', body })
export const updateArticle = (body) => javaRequest('/article', { method: 'PUT', body })
export const deleteArticle = (id) => javaRequest(`/article?id=${encodeURIComponent(id)}`, { method: 'DELETE' })
