"""接口集成测试：替换外部模型与 Java 调用，不消耗模型额度。"""
import unittest
from unittest.mock import patch
from fastapi.testclient import TestClient
import web_app as web

class IntegrationTests(unittest.TestCase):
    def setUp(self):
        web.sessions.clear()
        web.app.dependency_overrides[web.identity] = lambda: (1, 'test-only')
        self.client = TestClient(web.app)

    def tearDown(self):
        web.app.dependency_overrides.clear()

    def test_other_user_cannot_read_conversation(self):
        key = web.save_session(2, 'chat', object())
        result = self.client.post('/ai/notes/ask/stream', json={'thread_id': key, 'question': '你好'})
        self.assertEqual(result.status_code, 404)

    def test_stream_has_done(self):
        key = web.save_session(1, 'chat', object())
        with patch.object(web, 'stream_notes', return_value=iter(['第一段', '第二段'])):
            result = self.client.post('/ai/notes/ask/stream', json={'thread_id': key, 'question': '你好'})
        self.assertEqual(result.status_code, 200)
        self.assertIn('第一段', result.text)
        self.assertIn('"type":"done"', result.text)

    def test_empty_answer_is_422(self):
        result = self.client.post('/ai/quizzes/any/answer', json={'answer': '   '})
        self.assertEqual(result.status_code, 422)

    def test_note_permission(self):
        with patch.object(web, 'notes_for', return_value=[{'id': 3}]):
            result = self.client.post('/ai/notes/summarize', json={'note_id': 99})
        self.assertEqual(result.status_code, 404)

    def test_all_pages(self):
        pages = [{'items': [{'id': 1}], 'total': 2}, {'items': [{'id': 2}], 'total': 2}]
        with patch.object(web, 'java_get', side_effect=pages):
            self.assertEqual(len(web.notes_for('test-only')), 2)

if __name__ == '__main__':
    unittest.main()
