import unittest
from unittest.mock import patch
from fastapi import HTTPException
from pydantic import ValidationError
import web_app as web

class LogicTests(unittest.TestCase):
    def test_owner(self):
        key = web.save_session(1, 'chat', 'agent')
        self.assertEqual(web.get_session(key, 1, 'chat')[0], 'agent')
        with self.assertRaises(HTTPException) as result:
            web.get_session(key, 2, 'chat')
        self.assertEqual(result.exception.status_code, 404)

    def test_blank(self):
        with self.assertRaises(ValidationError):
            web.AnswerBody(answer='   ')

    def test_pages(self):
        with patch.object(web, 'java_get', side_effect=[
            {'items': [{'id': 1}], 'total': 2}, {'items': [{'id': 2}], 'total': 2}
        ]):
            self.assertEqual(web.notes_for('fake'), [{'id': 1}, {'id': 2}])

    def test_note_access(self):
        with patch.object(web, 'notes_for', return_value=[{'id': 3}]):
            with self.assertRaises(HTTPException) as result:
                web.owned_note(99, 'fake')
            self.assertEqual(result.exception.status_code, 404)
