# Adders / Removers generator

This plugin allows to generate adders and removers methods for class properties based on annotated type.

Recognized types are: 

* `array` 
* `ClassName[]` 
* `Doctrine\Common\Collections\Collection`

   **Note:** Any specific collection implementation is also supported, e.g. `Doctrine\Common\Collections\ArrayCollection`.

It is possible to generate only adders, only removers or both at the same time: plugin adds three menu items to class "Generate" context menu

![context menu](https://i.imgur.com/ExPnHNN.png)


*__Works in PhpStorm 9, 10, 2016.1, 2016.2, 2016.3, 2017.1, 2017.2__*

## Examples

### Before

```php
namespace App\Entity;

class Entity
{
    /**
     * @var array
     */
    protected $users;
}
```
### After

```php
namespace App\Entity;

class Entity
{
    /**
     * @var array
     */
    protected $users;

    /**
     * @param mixed $user
     */
    public function addUser($user)
    {
        $this->users[] = $user;
    }

    /**
     * @param mixed $user
     */
    public function removeUser($user)
    {
        if ($key = array_search($user, $this->users, true) !== false) {
            array_splice($this->users, $key, 1);
        }
    }
}
```
---
### Before

```php
namespace App\Entity;

class Entity
{
    /**
     * @var User[]
     */
    protected $users;
}
```

### After

```php
namespace App\Entity;

class Entity
{
    /**
     * @var User[]
     */
    protected $users;

    /**
     * @param \App\Entity\User $user
     */
    public function addUser(User $user)
    {
        $this->users[] = $user;
    }

    /**
     * @param \App\Entity\User $user
     */
    public function removeUser(User $user)
    {
        if ($key = array_search($user, $this->users, true) !== false) {
            array_splice($this->users, $key, 1);
        }
    }
}
```
---
### Before

```php
namespace App\Entity;

use Doctrine\Common\Collections\Collection;
use Doctrine\Common\Collections\ArrayCollection;

class Entity
{
    /**
     * @var User[]|Collection // or ArrayCollection
     */
    protected $users;
}
```

### After

```php
namespace App\Entity;

use Doctrine\Common\Collections\Collection;
use Doctrine\Common\Collections\ArrayCollection;

class Entity
{
    /**
     * @var User[]|Collection // or ArrayCollection
     */
    protected $users;

    /**
     * @param \App\Entity\User $user
     */
    public function addUser(User $user)
    {
        $this->users->add($user);
    }

    /**
     * @param \App\Entity\User $user
     */
    public function removeUser(User $user)
    {
        $this->users->removeElement($user);
    }
}
```

## Edit templates
To edit templates used for generating methods go to:
`Settings` -> `Editor` -> `File and Code Templates` -> `Other` -> `Adder/Remover`

![edit templates](https://i.imgur.com/Ss4NOHD.png)
