import { useAuthStore } from '../stores/auth'

// Java 的 code/message/data 与 HTTP 状态码是两层结果，必须分别判断。
export async function javaRequest(path, { method = 'GET', body } = {}) {
  const auth = useAuthStore()
  // FormData 由浏览器生成 multipart boundary，不能手动设置 JSON Content-Type。
  const form = body instanceof URLSearchParams || body instanceof FormData
  let response
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers: { ...(auth.token ? { Authorization: auth.token } : {}),
        ...(body && !form ? { 'Content-Type': 'application/json' } : {}) },
      body: body === undefined ? undefined : form ? body : JSON.stringify(body),
      signal: AbortSignal.timeout(15000),
    })
  } catch {
    throw new Error('无法连接后端或请求超时，请确认 Java 和前端开发服务已启动。')
  }
  if (response.status === 401) {
    auth.clear()
    if (location.pathname !== '/login') location.assign('/login')
    throw new Error('登录已过期，请重新登录。')
  }
  const result = await response.json().catch(() => null)
  if (!response.ok) throw new Error(result?.message || `后端请求失败（${response.status}）`)
  if (!result || typeof result.code !== 'number') throw new Error('后端响应格式不正确，应包含 code、message、data。')
  if (result.code !== 0) throw new Error(result.message || '操作失败')
  return result.data
}
