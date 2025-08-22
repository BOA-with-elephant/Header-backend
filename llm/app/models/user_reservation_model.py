from pydantic import BaseModel
from typing import List

class RevInfo(BaseModel):
    shopCode: int
    shopName: str
    menuCode: int
    menuCategoryCode: int
    menuName: str
    revCount: int

class Menu(BaseModel):
    shopCode: int
    menuRevCount: int
    menuName: str
    menuCode: int

class Shop(BaseModel):
    shopCode: int
    shopName: str
    menus: List[Menu]
    
class ShopCategory(BaseModel):
    categoryCode: int
    categoryName: str

class MenuCategory(BaseModel):
    menuCategoryName: str

class ShopAndMenuCategory(BaseModel):
    shopCategories: List[ShopCategory]
    menuCategories: List[MenuCategory]

class Request(BaseModel):
    query: str
