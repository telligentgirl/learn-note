"""出题规则由 MCP 资源提供。服务端只读取随项目发布的技能文件。"""
from pathlib import Path
from mcp.server.fastmcp import FastMCP

server = FastMCP('note-quiz')

@server.resource('skill://note-quiz/rules')
def quiz_rules() -> str:
    return (Path(__file__).parent / 'skills' / 'note-quiz' / 'SKILL.md').read_text(encoding='utf-8')

if __name__ == '__main__':
    server.run(transport='stdio')
