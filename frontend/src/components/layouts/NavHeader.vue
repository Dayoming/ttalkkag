<template>
    <nav class="navbar navbar-expand-lg bg-dark">
      <div class="container-fluid">
        <a class="navbar-brand text-white" href="#">API Tester</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse justify-content-end" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <router-link
                to="/test-api"
                class="nav-link text-white"
                active-class="active"
              >
                <i class="bi bi-arrow-through-heart-fill"></i>
                API Test
              </router-link>
            </li>
            <li class="nav-item">
              <router-link
                to="/projects"
                class="nav-link text-white"
                active-class="active"
              >
                <i class="bi bi-archive-fill"></i>
                Projects
              </router-link>
            </li>
            <li class="nav-item">
              <router-link
                to="/environment"
                class="nav-link text-white"
                active-class="active"
              >
                <i class="bi bi-globe-asia-australia"></i>
                Environment
              </router-link>
            </li>
            <li class="nav-item">
              <router-link
                to="/history"
                class="nav-link text-white"
                active-class="active"
              >
                <i class="bi bi-activity"></i>
                Reports/History
              </router-link>
            </li>
            <li class="nav-item">
              <router-link
                to="/dataset"
                class="nav-link text-white"
                active-class="active"
              >
                <i class="bi bi-egg"></i>
                Dataset
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
</template>

<script>
export default {
    name: "NavHeader",
};
</script>

<style scoped>
/* Navbar CSS Start */
#navbarNav {
    font-size: small;
}

.navbar {
    padding: 0;
}

.navbar a.nav-link {
    padding: 15px 20px;
}

.navbar a.nav-link:hover {
    background-color: black;
}

.navbar a.nav-link.active {
    background-color: black;
    color: white;
}
</style>
