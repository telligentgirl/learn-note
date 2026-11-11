from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class QuestionFeedback(BaseModel):
    """一道题的批改结果。"""

    model_config = ConfigDict(
        extra="forbid",
        str_strip_whitespace=True,
    )

    question_number: int = Field(ge=1)

    verdict: Literal[
        "correct",
        "partially_correct",
        "incorrect",
        "insufficient_evidence",
    ]

    explanation: str = Field(min_length=1)
    reference_answer: str = Field(min_length=1)


class QuizFeedback(BaseModel):
    """整次练习的批改结果。"""

    model_config = ConfigDict(
        extra="forbid",
        str_strip_whitespace=True,
    )

    overall_feedback: str = Field(min_length=1)
    items: list[QuestionFeedback] = Field(min_length=1)