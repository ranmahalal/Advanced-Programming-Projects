import yaml
from item import Item
from shopping_cart import ShoppingCart
from errors import *

class Store:
    def __init__(self, path):
        with open(path) as inventory:
            items_raw = yaml.load(inventory, Loader=yaml.FullLoader)['items']
        self._items = self._convert_to_item_objects(items_raw)
        self._shopping_cart = ShoppingCart()

    @staticmethod
    def _convert_to_item_objects(items_raw):
        return [Item(item['name'],
                     int(item['price']),
                     item['hashtags'],
                     item['description'])
                for item in items_raw]

    def get_items(self) -> list:
        return self._items
    
    def get_hashtags(self) -> list:
        """creats a list of all the hashtags of the items in this store's shopping cart"""
        hashtags_list = []
        for cur_item in self._shopping_cart.items.values():
            for hashtag in cur_item.hashtags:
                hashtags_list.append(hashtag)
        return hashtags_list

    def search_by_name(self, item_name: str) -> list:
        """returns a sorted list of the items that matched the searches"""
        #checks for matches and adds them to the returned list
        #only items that are not in the shopping cart
        matches_list = []
        for item in self._items:
                if item_name in item.name:
                    if item.name not in self._shopping_cart.items:
                        matches_list.append(item)
        #sorts the list: by common hashtags - descending, than by lexicographic  order
        tags = self.get_hashtags()

        def common_hashtags_count(item):
            """function to count the common hashtags for each item"""
            return sum(item.hashtags.count(hashtag) for hashtag in (tags))
                
        matches_list.sort(key=lambda item: (-common_hashtags_count(item), item.name))
        return matches_list


    def search_by_hashtag(self, hashtag: str) -> list:
        """returns a sorted list of the items that matched the hashtag searches"""
        #checks for matches and adds them to the returned list
        #only items that are not in the shopping cart
        matches_list = []
        for item in self._items:
                if hashtag in item.hashtags:
                    if item.name not in self._shopping_cart.items:
                        matches_list.append(item)
        #sorts the list: by common hashtags - descending, than by lexicographic  order
        tags = self.get_hashtags()

        def common_hashtags_count(item):
            """function to count the common hashtags for each item"""
            return sum(item.hashtags.count(hashtag) for hashtag in set(tags))
                
        matches_list.sort(key=lambda item: (-common_hashtags_count(item), item.name))
        return matches_list

    def add_item(self, item_name: str):
        """adds an item to the customer's shopping cart.
        throws en error if the given name matches more than 1 item,
        the item doesnt exist in the store, or it is already in the shopping cart"""
        #adds possible matches to a list. then operates according to the number 
        #of elements in the list.
        matches_list = []
        for item in self._items:
            if item_name in item.name:
                matches_list.append(item)
        if len(matches_list) > 1:
            raise TooManyMatchesError(f" item '{item_name}' matches to more than 1 item.")
        if len(matches_list) == 0:
            raise ItemNotExistError(f" item '{item_name}' does not exist.")
        #adds the item to the shopping if its not already there
        if matches_list[0].name in self._shopping_cart.items:
            raise ItemAlreadyExistsError(f" item '{item_name}' is already in the shopping cart.")
        ShoppingCart.add_item(self._shopping_cart, matches_list[0])

    def remove_item(self, item_name: str):
        """removes an item from the customer's shopping cart.
        throws en error if the given name matches more than 1 item,
        or if nthe item doesnt exist in the store"""
        #adds possible matches to a list. then operates according to the number 
        #of elements in the list.
        matches_list = []
        for item in self._items:
            if item_name in item.name:
                matches_list.append(item)
        if len(matches_list) > 1:
            raise TooManyMatchesError(f" item '{item_name}' matches to more than 1 item.")
        if len(matches_list) == 0:
            raise ItemNotExistError(f" item '{item_name}' does not exist.")
        #removes the item to the shopping cart - if it's there
        if matches_list[0] in self._shopping_cart.items.values():
            ShoppingCart.remove_item(self._shopping_cart, matches_list[0].name)
    
    def checkout(self) -> int:
        """computes the total price of the items in the customer's shopping cart"""
        return self._shopping_cart.get_subtotal()
