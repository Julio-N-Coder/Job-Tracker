export default function ThemeToggle() {
  const themes = [
    "default",
    "light",
    "dark",
    "cupcake",
    "bumblebee",
    "emerald",
    "corporate",
    "synthwave",
    "retro",
    "cyberpunk",
    "valentine",
    "halloween",
    "garden",
    "forest",
    "aqua",
    "lofi",
    "pastel",
    "fantasy",
    "wireframe",
    "black",
    "luxury",
    "dracula",
    "cmyk",
    "autumn",
    "business",
    "acid",
    "lemonade",
    "night",
    "coffee",
    "winter",
    "dim",
    "nord",
    "sunset",
    "caramellatte",
    "abyss",
    "silk",
  ];

  return (
    <div className="dropdown">
      <div tabIndex={0} role="button" className="btn m-1 text-xl">
        Theme
        <svg
          width="12px"
          height="12px"
          className="inline-block h-2 w-2 fill-current opacity-60"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 2048 2048"
        >
          <path d="M1799 349l242 241-1017 1017L7 590l242-241 775 775 775-775z"></path>
        </svg>
      </div>
      <ul
        tabIndex={0}
        className="dropdown-content bg-base-300 rounded-box z-1 w-52 p-2 shadow-2xl max-h-64 overflow-y-auto"
      >
        {themes.map((theme) => (
          <ThemeOption theme={theme} />
        ))}
      </ul>
    </div>
  );
}

function ThemeOption({ theme }: { theme: string }) {
  function saveTheme(theme: string) {
    {
      if (theme != "default") {
        localStorage.theme = theme;
        localStorage.preferedTheme = theme;
      } else if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
        localStorage.theme = "dark";
        delete localStorage.preferedTheme;
      } else {
        localStorage.theme = "light";
        delete localStorage.preferedTheme;
      }
    }
  }

  return (
    <li>
      <input
        type="radio"
        name="theme-dropdown"
        className="theme-controller btn btn-sm btn-block btn-ghost justify-start"
        aria-label={theme[0].toUpperCase() + theme.slice(1)}
        value={theme}
        onClick={() => saveTheme(theme)}
      />
    </li>
  );
}
