<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { javaRequest } from '../api/request'
import { listArticles, getArticle, addArticle, updateArticle, deleteArticle } from '../api/articles'
const auth = useAuthStore()
const router = useRouter()
const notes = ref([]), categories = ref([]), selected = ref(null)
const route = useRoute()
const tab = computed(() => route.path.endsWith('/chat') ? 'chat' : 'notes')
const busy = ref(false), error = ref('')
const page = ref(1), total = ref(0), pageSize = ref(10)
const createDialog = ref(null), detailDialog = ref(null), categoryDialog = ref(null)
function categoryLabel(id) { return categories.value.find(c => c.id === id)?.categoryName || '未分类' }
async function openNote(note, edit = false) { await choose(note); if (edit) editing.value = { ...selected.value }; detailDialog.value.showModal() }
const summary = ref(null), quiz = ref(null), answer = ref('')
const thread = ref(''), messages = ref([]), question = ref('')
const aiMode = ref('chat'), practiceNotes = ref([]), practiceNoteId = ref('')
async function openPractice() {
  const items = []
  for (let pageNum = 1; ; pageNum++) {
    const data = await listArticles({ pageNum, pageSize: 50 })
    items.push(...data.items)
    if (!data.items.length || items.length >= data.total) break
  }
  practiceNotes.value = items
  if (!items.some(note => note.id === practiceNoteId.value)) practiceNoteId.value = ''
  aiMode.value = 'practice'
}
async function startChatPractice() {
  await openNote({ id: practiceNoteId.value })
  await createQuiz()
}
const draft = ref({ title: '', content: '', categoryId: '', state: '草稿' })
const categoryName = ref(''), categoryAlias = ref('')
const categoryTarget = ref(null)
function openCategories(target = null) {
  // 叠加分类弹窗，不关闭笔记弹窗，所以标题和正文不会丢失。
  categoryTarget.value = target
  categoryName.value = ''; categoryAlias.value = ''; error.value = ''
  categoryDialog.value.showModal()
}
async function createCategory() {
  const name = categoryName.value.trim(), alias = categoryAlias.value.trim()
  if (!name || !alias) throw new Error('请填写分类名称和别名。')
  if (categories.value.some(c => c.categoryName === name || c.categoryAlias === alias)) {
    throw new Error('分类名称或别名已存在，请直接选择或使用其他名称。')
  }
  const previousIds = new Set(categories.value.map(c => c.id))
  await request('/api/category', 'POST', { categoryName: name, categoryAlias: alias })
  categories.value = await request('/api/category')
  // 新增接口没有返回 ID：刷新分类列表，定位刚创建的分类再选中。
  const created = categories.value.find(c => !previousIds.has(c.id) && c.categoryName === name && c.categoryAlias === alias)
  if (categoryTarget.value && created) {
    categoryTarget.value.categoryId = created.id
    categoryDialog.value.close()
  }
  categoryName.value = ''; categoryAlias.value = ''
  success.value = created ? `已创建分类“${name}”。` : '分类已创建，请在列表中选择。'
}
const editing = ref(null)
const success = ref(''), allTotal = ref(null), loaded = ref(false)
async function resetFilters() {
  filterCategory.value = ''; filterState.value = ''; page.value = 1
  await loadNotes()
}
const filterCategory = ref(''), filterState = ref('')

// 所有请求携带登录 Token。Java 返回 Result 包装，Python 直接返回数据。
async function request(path, method = 'GET', body) {
  if (path.startsWith('/api')) return javaRequest(path.slice(4), { method, body })
  const response = await fetch(path, {
    method, headers: { Authorization: auth.token, 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (response.status === 401) { auth.clear(); await router.push('/login'); throw new Error('登录已失效') }
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(typeof data.detail === 'string' ? data.detail : `请求失败：${response.status}`)
  if (path.startsWith('/api')) {
    if (data.code !== 0) throw new Error(data.message || '操作失败')
    return data.data
  }
  return data
}
// 统一防重复操作和错误提示；失败时保留输入，便于修改后重试。
async function run(action) {
  if (busy.value) return
  busy.value = true; error.value = ''; success.value = ''
  try { await action() } catch (e) { error.value = e.message } finally { busy.value = false }
}
async function loadNotes() {
  const params = new URLSearchParams({ pageNum: page.value, pageSize: pageSize.value })
  if (filterCategory.value) params.set('categoryId', filterCategory.value)
  if (filterState.value) params.set('state', filterState.value)
  const data = await listArticles(params)
  notes.value = data.items; total.value = data.total
  loaded.value = true
  if (!filterCategory.value && !filterState.value) allTotal.value = data.total
  else allTotal.value = (await listArticles({ pageNum: 1, pageSize: 1 })).total
}
onMounted(() => run(async () => { await loadNotes(); categories.value = await request('/api/category') }))
async function choose(note) {
  // 详情必须调用详情接口，不能用列表缓存冒充详情请求。
  selected.value = note ? await getArticle(note.id) : null
  editing.value = null
  summary.value = null; quiz.value = null; answer.value = ''
}
async function addNote() {
  if (!/^\S{1,10}$/.test(draft.value.title) || !draft.value.content.trim() || !draft.value.categoryId) throw new Error('标题须为 1～10 个非空白字符，请填写正文并选择分类。')
  await addArticle({ ...draft.value, categoryId: Number(draft.value.categoryId) })
  draft.value = { title: '', content: '', categoryId: '', state: '草稿' }
  await resetFilters()
  createDialog.value.close()
  success.value = '笔记已保存，已清除筛选并刷新列表。'
}
async function saveEdit() {
  const note = editing.value
  if (!/^\S{1,10}$/.test(note.title) || !note.content.trim()) throw new Error('请填写有效的标题和正文。')
  await updateArticle({ id: note.id, title: note.title, content: note.content, categoryId: Number(note.categoryId), state: note.state })
  await choose(note); editing.value = null; await loadNotes()
  success.value = '修改已保存。'
}
async function removeNote(note) {
  if (!window.confirm(`确定删除「${note.title}」？`)) return
  await deleteArticle(note.id)
  if (selected.value?.id === note.id) await choose(null)
  page.value = 1; await loadNotes()
}
async function newChat() {
  const data = await request('/ai/conversations', 'POST')
  thread.value = data.thread_id; messages.value = []
}
async function send() {
  if (!question.value.trim()) throw new Error('请输入问题。')
  if (!thread.value) await newChat()
  const text = question.value.trim()
  messages.value.push({ role: '你', text }, { role: '助手', text: '' })
  const index = messages.value.length - 1
  question.value = ''
  const response = await fetch('/ai/notes/ask/stream', {
    method: 'POST', headers: { Authorization: auth.token, 'Content-Type': 'application/json' },
    body: JSON.stringify({ question: text, thread_id: thread.value }),
  })
  if (!response.ok) {
    if (response.status === 401) { auth.clear(); await router.push('/login') }
    const data = await response.json().catch(() => ({}))
    throw new Error(data.detail || '请求失败，请新建对话后重试。')
  }
  // 网络块不等于 JSON 行：先缓存残片，再按换行拆出完整事件。
  const reader = response.body.getReader(), decoder = new TextDecoder()
  let buffer = '', done = false
  function event(line) {
    if (!line.trim()) return
    const data = JSON.parse(line)
    if (data.type === 'text') messages.value[index].text += data.content
    if (data.type === 'done') done = true
    if (data.type === 'error') throw new Error(data.message)
  }
  try {
    while (true) {
      const part = await reader.read()
      if (part.done) break
      buffer += decoder.decode(part.value, { stream: true })
      const lines = buffer.split('\n'); buffer = lines.pop()
      lines.forEach(event)
    }
    buffer += decoder.decode(); event(buffer)
    if (!done) throw new Error('连接中断，回答未完成。')
  } finally { await reader.cancel(); reader.releaseLock() }
}
async function createQuiz() {
  quiz.value = await request('/ai/quizzes', 'POST', { note_id: selected.value.id })
  answer.value = ''
}
async function submitQuiz() {
  if (!answer.value.trim()) throw new Error('答案不能为空。')
  const result = await request(`/ai/quizzes/${quiz.value.thread_id}/answer`, 'POST', { answer: answer.value })
  quiz.value = { ...quiz.value, ...result }
}
const labels = { correct: '正确', partially_correct: '部分正确', incorrect: '错误', insufficient_evidence: '依据不足' }
</script>

<template>
  <main class="panel">
    <div class="panel-heading"><h2>{{ tab === 'notes' ? '笔记管理' : 'AI 学习助手' }}</h2><button v-if="tab === 'notes'" :disabled="busy" @click="createDialog.showModal()">＋ 新建笔记</button><button v-else-if="aiMode === 'chat'" :disabled="busy" @click="run(newChat)">新建对话</button></div>
    <p v-if="busy" class="notice" role="status">正在处理，请稍候…</p><p v-if="error" class="error" role="alert">{{ error }}</p><p v-if="success" class="success" role="status">{{ success }}</p>
    <fieldset :disabled="busy">
      <section v-if="tab === 'notes'">
        <!-- 筛选区与表格共用分页接口，重置回到第一页。 -->
        <form class="filters" @submit.prevent="run(async () => { page = 1; await loadNotes() })">
          <label>笔记分类：<select v-model="filterCategory"><option value="">全部分类</option><option v-for="c in categories" :key="c.id" :value="c.id">{{ c.categoryName }}</option></select></label>
          <label>发布状态：<select v-model="filterState"><option value="">全部状态</option><option>草稿</option><option>已发布</option></select></label>
          <button>搜索</button><button type="button" class="plain" @click="run(resetFilters)">重置</button><button type="button" class="plain" @click="openCategories()">管理分类</button>
        </form>
        <div class="table-scroll"><table><thead><tr><th>笔记标题</th><th>分类</th><th>创建时间</th><th>状态</th><th class="actions">操作</th></tr></thead><tbody>
          <tr v-for="note in notes" :key="note.id"><td><button class="link" @click="run(() => openNote(note))">{{ note.title }}</button></td><td>{{ categoryLabel(note.categoryId) }}</td><td>{{ note.createTime?.replace('T', ' ') || '—' }}</td><td><span :class="['tag', { published: note.state === '已发布' }]">{{ note.state }}</span></td><td class="actions"><button class="icon edit" title="编辑笔记" aria-label="编辑笔记" @click="run(() => openNote(note, true))">✎</button><button class="icon danger" title="删除笔记" aria-label="删除笔记" @click="run(() => removeNote(note))">×</button></td></tr>
          <tr v-if="loaded && !notes.length"><td colspan="5" class="empty">{{ allTotal ? '没有符合筛选条件的笔记，请重置筛选。' : '还没有笔记，点击右上角新建笔记开始记录。' }}</td></tr>
        </tbody></table></div>
        <div class="pagination"><span>共 {{ total }} 条</span><select v-model.number="pageSize" aria-label="每页条数" @change="run(async () => { page = 1; await loadNotes() })"><option :value="10">10条/页</option><option :value="20">20条/页</option><option :value="50">50条/页</option></select><button class="plain" :disabled="page <= 1" @click="run(async () => { page--; await loadNotes() })">‹</button><span class="current">{{ page }}</span><button class="plain" :disabled="page * pageSize >= total" @click="run(async () => { page++; await loadNotes() })">›</button></div>
      </section>
      <section v-else class="chat">
        <div class="mode-switch" role="group" aria-label="学习功能">
          <button type="button" :class="{ plain: aiMode !== 'chat' }" :aria-pressed="aiMode === 'chat'" @click="aiMode = 'chat'">笔记问答</button>
          <button type="button" :class="{ plain: aiMode !== 'practice' }" :aria-pressed="aiMode === 'practice'" @click="run(openPractice)">笔记练习</button>
        </div>
        <form v-if="aiMode === 'practice'" class="practice-picker" @submit.prevent="run(startChatPractice)">
          <label>选择出题笔记
            <select v-model="practiceNoteId" required>
              <option value="" disabled>请选择笔记</option>
              <option v-for="note in practiceNotes" :key="note.id" :value="note.id">{{ note.title }} · #{{ note.id }}</option>
            </select>
          </label>
          <p v-if="!practiceNotes.length">请先在笔记管理中创建笔记。</p>
          <button :disabled="!practiceNoteId">生成练习</button>
        </form>
        <div v-else>
          <article v-for="(message, i) in messages" :key="i">
            <strong>{{ message.role }}</strong><p class="prose">{{ message.text }}</p>
          </article>
          <form @submit.prevent="run(send)">
            <label>你的问题<textarea v-model="question" required placeholder="例如：根据我的笔记介绍 HashMap" /></label>
            <button>发送</button>
          </form>
        </div>
      </section>
    </fieldset>
    <!-- 原生 dialog 提供模态焦点约束和 Esc 关闭，不遮挡主表格布局。 -->
    <dialog ref="createDialog" @cancel="busy && $event.preventDefault()"><div class="dialog-head"><h2>新建笔记</h2><button class="plain" :disabled="busy" @click="createDialog.close()">关闭</button></div><p v-if="error" class="error" role="alert">{{ error }}</p><fieldset :disabled="busy"><form @submit.prevent="run(addNote)"><label>标题<input v-model="draft.title" maxlength="10" required /></label><label>正文<textarea v-model="draft.content" required /></label><label>分类<select v-model="draft.categoryId" required><option value="">请选择</option><option v-for="c in categories" :key="c.id" :value="c.id">{{ c.categoryName }}</option></select></label><button type="button" class="plain custom-category" @click="openCategories(draft)">＋ 自定义分类</button><select v-model="draft.state"><option>草稿</option><option>已发布</option></select><button>保存笔记</button></form></fieldset></dialog>
    <dialog ref="detailDialog" @cancel="busy && $event.preventDefault()"><div class="dialog-head"><h2>笔记详情与练习</h2><button class="plain" :disabled="busy" @click="detailDialog.close()">关闭</button></div><p v-if="error" class="error" role="alert">{{ error }}</p><p v-if="busy" role="status">正在处理，请稍候…</p><fieldset :disabled="busy"><section v-if="selected">
          <h2>{{ selected.title }}</h2><p class="prose">{{ selected.content }}</p>
          <!-- flex gap 让按钮横向排列，换行时仍保留间距。 -->
          <div class="note-actions"><button @click="editing = { ...selected }">编辑笔记</button>
          <form v-if="editing" @submit.prevent="run(saveEdit)"><h3>编辑笔记</h3><label>标题<input v-model="editing.title" maxlength="10" required /></label><label>正文<textarea v-model="editing.content" required /></label><label>分类<select v-model="editing.categoryId" required><option v-for="c in categories" :key="c.id" :value="c.id">{{ c.categoryName }}</option></select></label><button type="button" class="plain custom-category" @click="openCategories(editing)">＋ 自定义分类</button><label>状态<select v-model="editing.state"><option>草稿</option><option>已发布</option></select></label><div class="form-actions"><button>保存修改</button><button type="button" class="plain" @click="editing = null">取消</button></div></form>
          <button @click="run(async () => { summary = await request('/ai/notes/summarize', 'POST', { note_id: selected.id }) })">生成摘要</button><button @click="run(createQuiz)">生成练习</button></div>
          <section v-if="summary"><h3>笔记摘要</h3><p>{{ summary.summary }}</p><ul><li v-for="point in summary.key_points" :key="point">{{ point }}</li></ul><h4>复习问题</h4><ol><li v-for="q in summary.review_questions" :key="q">{{ q }}</li></ol></section>
          <section v-if="quiz"><h3>笔记练习</h3><p class="prose">{{ quiz.questions }}</p><form v-if="quiz.status === 'waiting_for_answer'" @submit.prevent="run(submitQuiz)"><label>你的答案<textarea v-model="answer" required /></label><button>提交批改</button></form><section v-if="quiz.grading"><h3>批改反馈</h3><p>{{ quiz.grading.overall_feedback }}</p><article v-for="item in quiz.grading.items" :key="item.question_number"><h4>第 {{ item.question_number }} 题 · {{ labels[item.verdict] }}</h4><p>{{ item.explanation }}</p><p>参考答案：{{ item.reference_answer }}</p></article><h3>待复习题目</h3><p v-if="!quiz.review_items.length">本次没有待复习题目。</p><p v-for="item in quiz.review_items" :key="item.question_number">第 {{ item.question_number }} 题：{{ item.reference_answer }}</p></section></section>
        </section></fieldset></dialog>
    <dialog ref="categoryDialog" @cancel="busy && $event.preventDefault()"><div class="dialog-head"><h2>笔记分类</h2><button class="plain" :disabled="busy" @click="categoryDialog.close()">关闭</button></div><p v-if="error" class="error">{{ error }}</p><table><thead><tr><th>分类名称</th><th>别名</th></tr></thead><tbody><tr v-for="c in categories" :key="c.id"><td>{{ c.categoryName }}</td><td>{{ c.categoryAlias }}</td></tr></tbody></table><h3>新建分类</h3><fieldset :disabled="busy"><form @submit.prevent="run(createCategory)"><input v-model="categoryName" placeholder="分类名称" required aria-label="分类名称" /><input v-model="categoryAlias" placeholder="分类别名" required aria-label="分类别名" /><button>创建分类</button></form></fieldset></dialog>
  </main>
</template>
<style scoped>
.mode-switch { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.practice-picker { margin: 16px 0; padding: 12px 16px; border: 1px solid #ebeef5; border-radius: 6px; }
.practice-picker select { display: block; width: 100%; margin-top: 8px; }
.custom-category { margin: -4px 0 16px; }
/* 使用 gap 而非文字空格，避免按钮贴合，并兼容窄屏自动换行。 */
.note-actions, .form-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; }
.note-actions { margin: 20px 0; }
.note-actions > form { flex-basis: 100%; min-width: 0; }
.form-actions { margin: 16px 0 4px; }

.panel{background:white;min-height:70vh;border:1px solid #ebeef5;color:#606266;font-size:14px}.panel-heading{height:66px;display:flex;align-items:center;justify-content:space-between;padding:0 24px;border-bottom:1px solid #ebeef5}h2{font-size:16px;font-weight:500;color:#303133}h3{font-size:15px}fieldset{border:0;margin:0;padding:0;min-width:0}button{border:1px solid #409eff;background:#409eff;color:white;border-radius:4px;padding:8px 16px;cursor:pointer;font:inherit}button:disabled{opacity:.45;cursor:not-allowed}.plain{background:white;border-color:#dcdfe6;color:#606266}.filters{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding:24px}.filters label{display:flex;align-items:center;gap:10px;margin:0}.filters select{width:180px;margin:0}input,select,textarea{border:1px solid #dcdfe6;border-radius:4px;padding:9px 12px;background:white;font:inherit;color:#606266}label{display:block;margin:16px 0}dialog input,dialog textarea,dialog select,.chat textarea{display:block;width:100%;margin-top:8px}textarea{min-height:140px;resize:vertical}.table-scroll{overflow-x:auto;padding:0 24px}table{width:100%;border-collapse:collapse;text-align:left}th{font-weight:500;color:#909399}td,th{padding:20px 12px;border-bottom:1px solid #ebeef5}td:first-child{width:38%}tbody tr:hover{background:#fafcff}.link{border:0;padding:0;background:none;color:#409eff;text-align:left}.actions{white-space:nowrap;text-align:center}.icon{border-radius:50%;width:30px;height:30px;padding:0;font-size:20px;margin:0 5px}.edit{color:#409eff;background:#ecf5ff;border-color:#d9ecff}.danger{color:#f56c6c;background:#fef0f0;border-color:#fde2e2}.tag{background:#f4f4f5;color:#909399;padding:4px 8px;border-radius:4px;font-size:12px}.published{background:#f0f9eb;color:#67c23a}.pagination{display:flex;justify-content:flex-end;align-items:center;gap:12px;padding:26px 24px}.current{padding:8px 13px;background:#409eff;color:white;border-radius:3px}.empty{text-align:center;padding:80px 20px;color:#909399}.error,.success,.notice{margin:16px 24px;padding:12px;border-radius:4px;overflow-wrap:anywhere}.error{background:#fef0f0;color:#c45656}.success{background:#f0f9eb;color:#529b2e}.notice{background:#ecf5ff;color:#337ecc}dialog{width:min(720px,94vw);max-height:88vh;overflow-y:auto;border:0;border-radius:8px;padding:24px;box-shadow:0 12px 60px #0003;color:#606266}dialog::backdrop{background:#0006}.dialog-head{display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #eee;padding-bottom:12px}dialog form>button{margin:8px 8px 8px 0}.prose{white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.8}.chat{padding:24px;max-width:1000px;margin:auto}article{padding:16px;background:#f5f7fa;margin:16px 0;border-radius:5px}@media(max-width:700px){.filters{padding:14px}.filters label{width:100%}.filters select{flex:1}.table-scroll{padding:0 10px}table{min-width:640px}.pagination{padding:20px 10px;gap:8px}}
</style>
